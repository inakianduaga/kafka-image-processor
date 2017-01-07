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
import com.sksamuel.{scrimage => ImgLib}


class Kafka {
}

object Kafka {

  def main(args:Array[String]): Unit = {
  }

  // Keep main thread alive indefinitely
  val keepAlive = new CountDownLatch(1)
  keepAlive.await()

  urlToBinaryProcessor()
  applyFilterProcessor()

  def urlToBinaryProcessor() = {

    val producer = KafkaProducer(
      Conf(new StringSerializer(), new ByteArraySerializer(), bootstrapServers = Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
    )

    def send(value: Array[Byte]) = producer.send(KafkaProducerRecord(topic="Images.Binaries", key=None, value))

    val consumer = {
      val config: JavaProperties = {
        val props = new JavaProperties()
        props.put("bootstrap.servers", Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
        props.put("group.id", "kafka-image-processor-consumer-group-1")
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
        .map(futureResponse => futureResponse.map(
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
        props.put("group.id", "kafka-image-processor-consumer-group-1")
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer")
        props
      }

      new KafkaConsumer[String, String](config)
    }

    consumer.subscribe(List("Images.Binaries").asJava)

    val producer = KafkaProducer(
      Conf(new StringSerializer(), new ByteArraySerializer(), bootstrapServers = Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
    )

    def send(value: Array[Byte]) = producer.send(KafkaProducerRecord(topic="Images.Urls", key=None, value))

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

}


