package com.inakianduaga

import java.io.ByteArrayInputStream

import com.inakianduaga.services.HttpClient.{get => httpGet}
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.{Properties => JavaProperties}

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
    urlToBinaryProcessorUsingStreams()
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
      props.put("ZOOKEEPER_CONNECT_CONFIG", Properties.envOrElse("ZOOKEEPER_ENDPOINT", "localhost:2181"))
      props.put("zookeeper.connect.config", Properties.envOrElse("ZOOKEEPER_ENDPOINT", "localhost:2181"))
      props.put("zookeeper.connect", Properties.envOrElse("ZOOKEEPER_ENDPOINT", "localhost:2181"))
      props.put("ZOOKEEPER_CONNECT", Properties.envOrElse("ZOOKEEPER_ENDPOINT", "localhost:2181"))

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


