package com.inakianduaga


//import org.apache.kafka.clients.consumer.KafkaConsumer
//import java.util.{Properties => JavaProperties}
import com.inakianduaga.services.HttpClient.{get => httpGet}

import com.sksamuel.{scrimage => ImgLib}
import java.io.ByteArrayInputStream

import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}
import scala.util.Properties
import akka.stream.scaladsl.Sink
import akka.kafka.scaladsl.Consumer
import akka.kafka.ConsumerSettings
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.kafka.Subscriptions

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

    Consumer.committableSource(consumerSettings, Subscriptions.topics("Images.Urls"))
      .mapAsync(1)(message => httpGet(message.record.value()))
      .filter(response => response.status == 200)
      .map(response => response.bodyAsBytes)
      .map(imgBytes => new ByteArrayInputStream(imgBytes))
      .map(imgInputStream => ImgLib.Image.fromStream(imgInputStream))
      .map(image => image.filter(ImgLib.filter.GrayscaleFilter))
      .runWith(Sink.ignore)
  }

}


