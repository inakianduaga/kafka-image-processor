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
    imageUrls$
      .mapAsync(1)(message => httpGet(message.record.value()).map(response => (message.committableOffset, response)))
      .filter(response => response._2.status == 200)
      .map(response => (response._1, response._2.bodyAsBytes))
      .map(imgBytes => (imgBytes._1, new ByteArrayInputStream(imgBytes._2)))
      .map(imgInputStream => (imgInputStream._1, ImgLib.Image.fromStream(imgInputStream._2)))
      .map(image => (image._1, image._2.filter(ImgLib.filter.GrayscaleFilter)))
      .map(image => (image._1, new ProducerRecord[String, String]("Images.Filtered", image._2.bytes.toString)))
      .map { pair => ProducerMessage.Message(pair._2, pair._1)}
      .runWith(Producer.commitableSink(producerSettings))

  }

}


