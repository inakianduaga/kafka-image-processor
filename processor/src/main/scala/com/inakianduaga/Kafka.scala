package com.inakianduaga

import java.io.ByteArrayInputStream

import com.inakianduaga.services.HttpClient.{get => httpGet}
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.{Properties => JavaProperties}

import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import cakesolutions.kafka.KafkaProducer.Conf
import org.apache.kafka.common.serialization.{ StringSerializer, StringDeserializer, ByteArraySerializer, ByteArrayDeserializer}

import scala.util.Properties
import scala.collection.JavaConverters._
import java.util.concurrent.CountDownLatch

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import com.sksamuel.{scrimage => ImgLib}

// Kafka streams import
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.processor.TopologyBuilder
import org.apache.kafka.streams.processor.Processor
import org.apache.kafka.streams.processor.ProcessorContext
import org.apache.kafka.streams.processor.ProcessorSupplier

class Kafka {
}

object Kafka {

  def main(args:Array[String]): Unit = {
  }

  urlToBinaryProcessorUsingStreams()
  urlToBinaryProcessor()
  applyFilterProcessor()

  // Keep main thread alive indefinitely
  val keepAlive = new CountDownLatch(1)
  keepAlive.await()

  def urlToBinaryProcessor() = {

    val producer = KafkaProducer(
      Conf(new StringSerializer(), new ByteArraySerializer(), bootstrapServers = Properties.envOrElse("KAFKA_ENDPOINT", "kafka:9092"))
    )

    def send(value: Array[Byte]) = producer.send(KafkaProducerRecord(topic="Images.Binaries", key=None, value))
    println("testing sending some bytes")
    send("123".getBytes)

    val consumer = {
      val config: JavaProperties = {
        val props = new JavaProperties()
        props.put("bootstrap.servers", Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
        props.put("group.id", "kafka-image-url-processor-consumer-group")
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

        props
      }
      new KafkaConsumer[String, String](config)
    }

    consumer.subscribe(List("Images.Urls").asJava)

    // Fetch records continuously
    while(true) {
      val imgUrlsRecords = consumer.poll(1000).asScala

      imgUrlsRecords
        .map(imageUrlRecord => httpGet(imageUrlRecord.value))
        .foreach(futureResponse => futureResponse.foreach(
          response => response.status match {
            case 200 => send(response.bodyAsBytes)
            case _ => println(s"Failed to download - Status ${response.status}" )
          }
        ))
    }

  }

  /**
    * Process image binary stream into
    */
  def applyFilterProcessor() = {

    val greyscaleFilter = ImgLib.filter.GrayscaleFilter

    val consumer = {

      val config: JavaProperties = {
        val props = new JavaProperties()
        props.put("bootstrap.servers", Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
        props.put("group.id", "kafka-image-binary-processor-consumer-group")
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")
        props
      }

      new KafkaConsumer[String, String](config)
    }

    consumer.subscribe(List("Images.Binaries").asJava)

    val producer = KafkaProducer(
      Conf(new StringSerializer(), new ByteArraySerializer(), bootstrapServers = Properties.envOrElse("KAFKA_ENDPOINT", "kafka:9092"))
    )

    def send(value: Array[Byte]) = producer.send(KafkaProducerRecord(topic="Images.Processed", key=None, value))

    // Fetch records continuously
    while(true) {
      val imgBinaryRecords = consumer.poll(1000).asScala

      imgBinaryRecords
        .map(record => new ByteArrayInputStream(record.value.getBytes))
        .map(inputStream => ImgLib.Image.fromStream(inputStream))
        .map(image => image.filter(greyscaleFilter))
        .foreach(processedImage => send(processedImage.bytes))
    }

  }






  class UrlProcessor[K, V] extends (Processor[K, V]) {

    def init(context: ProcessorContext) = {}

    def process(key: K, value: V) = {
      println(s"Stream - Dummy processor received value: $value")
    }

    def punctuate(timestamp: Long) = {}

    def close() = {}
  }

  class UrlProcessorSupplierBuilder[K, V] extends (ProcessorSupplier[K, V]) {
    def get() = new UrlProcessor[K, V]()
  }

  def urlToBinaryProcessorUsingStreams() = {

    val builder: TopologyBuilder = new TopologyBuilder()
    val processorSupplier = new UrlProcessorSupplierBuilder[String, String]()

    builder
      .addSource("Source", "Images.Urls")
      .addProcessor("Process", processorSupplier, "Source")
      .setApplicationId("kafka-image-binary-processor")

    val config: StreamsConfig = {
      val props = new JavaProperties()
      props.put("BOOTSTRAP_SERVERS_CONFIG", Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
      props.put("bootstrap.servers", Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
      props.put("application.id", "kafka-image-binary-processor")
      new StreamsConfig(props)
    }

    val streams: KafkaStreams = new KafkaStreams(builder, config)

    // Start the Kafka Streams threads
    streams.start()
  }

}


