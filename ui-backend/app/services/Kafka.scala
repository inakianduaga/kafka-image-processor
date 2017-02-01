package services

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.clients.producer.ProducerRecord
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.scaladsl.{Sink, Source}
import org.apache.kafka.clients.consumer.ConsumerConfig

import scala.util.Properties

@Singleton
class Kafka @Inject() (configuration: play.api.Configuration) {

  val config = ConfigFactory.load()
  implicit val system = ActorSystem.create("kafka-image-processor", config)
  implicit val mat = ActorMaterializer()

  val producerSettings = ProducerSettings(system, new StringSerializer(), new StringSerializer())
    .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))

  // Subscribe to Kafka topics on initialization
  subscribeToKafkaTopics()

  def subscribeToKafkaTopics() = {

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
      .withGroupId("kafka-processed-image-reader")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

    // TODO: Add subscription to Kafka topics here to read processed images, which we will then push to actors
    Consumer.committableSource(consumerSettings, Subscriptions.topics("Images.Filtered"))
      .map(message => println(s"Received filtered message w/ value ${message.record.value()}"))
      .runWith(Sink.ignore)
  }

  def send(value: String) = Source.single(new ProducerRecord[String, String]("Images.Urls", value))
      .runWith(Producer.plainSink(producerSettings))

  // HACK: Save instance into object companion for retrieving it without needing dependency injection
  Kafka.setInstance(this)
}

object Kafka {

  var kafkaInstance: Option[Kafka] = None

  def setInstance(instance: Kafka) = {
    kafkaInstance = Some(instance)
  }

  def getInstance() = kafkaInstance.get
}
