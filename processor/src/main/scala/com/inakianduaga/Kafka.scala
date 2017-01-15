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
import org.apache.kafka.common.serialization.Serdes
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

    var context: Option[ProcessorContext] = None

    @Override
    def init(context: ProcessorContext) = {
      this.context = Some(context)
    }

    @Override
    def process(key: K, value: V) = {
      println(s"Stream - Dummy processor received value: $value")

      httpGet(value.asInstanceOf[String])
        .foreach(
          response => response.status match {
            case 200 => {
              println(s"downloaded image url, ready to push binary ${response.body.size}")
//              send(response.bodyAsBytes)
              // Forwards a key/value pair to the downstream processors
              context.get.forward("123", "234")
              context.get.commit()
            }
            case _ => println(s"Failed to download - Status ${response.status}" )
          }
        )

    }

    @Override
    def punctuate(timestamp: Long) = {}

    @Override
    def close() = {}
  }

  class UrlProcessorSupplierBuilder[K, V] extends (ProcessorSupplier[K, V]) {
    def get() = new UrlProcessor[K, V]()
  }

  def urlToBinaryProcessorUsingStreams() = {
    val builder: TopologyBuilder = new TopologyBuilder()
    val processorSupplier = new UrlProcessorSupplierBuilder[String, String]()

    builder
      .setApplicationId("kafka-image-binary-processor")
      .addSource("Source", "Images.Urls")
      .addProcessor("DownloadImages", processorSupplier, "Source")
      .addSink("ImageBinaries", "Images.Binaries", Serdes.String().serializer(), Serdes.ByteArray().serializer(), "DownloadImages")

    val config: StreamsConfig = {
      val props = new JavaProperties()
      props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
      props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-image-binary-processor")
      // Default serde for keys of data records (here: built-in serde for String type)
      props.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass.getName)
      // Default serde for keys of data records (here: built-in serde for String type)
      props.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass.getName)
      new StreamsConfig(props)
    }

    val streams: KafkaStreams = new KafkaStreams(builder, config)

    streams.start()
  }

}


