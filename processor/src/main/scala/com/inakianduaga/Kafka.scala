package com.inakianduaga


//import org.apache.kafka.clients.consumer.KafkaConsumer
//import java.util.{Properties => JavaProperties}
import com.inakianduaga.services.HttpClient.{get => httpGet}
import com.sksamuel.{scrimage => ImgLib}
import java.io.ByteArrayInputStream

import akka.NotUsed
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}

import scala.util.Properties
import akka.stream.scaladsl.{Flow, RunnableGraph, Sink, Source}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Producer
import akka.kafka.{ConsumerMessage, ConsumerSettings, ProducerMessage, ProducerSettings, Subscriptions}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContext.Implicits.global

class Kafka {
}

object Kafka {

  def main(args:Array[String]): Unit = {
    urlToBinaryProcessorUsingStreams
  }

  def urlToBinaryProcessorUsingStreams = {

    val config = ConfigFactory.load()
    implicit val system = ActorSystem.create("kafka-image-processor", config)
    implicit val mat = ActorMaterializer()

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
      .withGroupId("kafka-image-binary-processor")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

    val producerSettings = ProducerSettings(system, new StringSerializer(), new StringSerializer())
      .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))

    val imageUrls$ = Consumer.committableSource(consumerSettings, Subscriptions.topics("Images.Urls"))
    val imageCommitableOffsets$ = imageUrls$.map(message => message.committableOffset)

    imageCommitableOffsets$.runWith(Sink.ignore).onFailure {
      case x: Throwable => println(s"Exception Caught: ${x.getMessage}")
      case _ => println(s"Exception Caught - GENERIC")
    }

    // Download & Filter URL Stream
    val filteredImages$ = imageUrls$
      .mapAsync(1)(message => httpGet(message.record.value()))
      .filter(response => response.status == 200)
      .map(response => response.bodyAsBytes)
            .map(response => {
              println(s"IMAGE DOWNLOADED RESPONSE: ${response.length}")
              response
            })
      .map(imgBytes => new ByteArrayInputStream(imgBytes))
      .map(imgInputStream => {
        println("BEFORE READING FROM STREAM")
        ImgLib.Image.fromStream(imgInputStream)
      })
      .map(image => {
        println("BEFORE APPLYING FILTER")
        image.filter(ImgLib.filter.GrayscaleFilter)
      })
      .map(image => {
        println(s"CONVERTED IMAGE TO GREYSCALE, LENGTH: ${image.bytes.length}")
        image
      })
      // Convert images into new topic producer records & hook with producer
      .map(image => {
        println(s"RUN FIRST CHING OF PRODUCER RECORD")
        new ProducerRecord[String, String]("Images.Filtered", image.bytes.toString)
      })
      .zip(imageCommitableOffsets$)
      .map { case (producerRecord, offset) => ProducerMessage.Message(producerRecord, offset)}
      .runWith(Producer.commitableSink(producerSettings))
      .onFailure {
        case x: Throwable => println(s"Exception Caught: ${x.getMessage}")
        case _ => println(s"Exception Caught - GENERIC")
      }

  }

}


