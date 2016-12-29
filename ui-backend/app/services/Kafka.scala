package services

import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import cakesolutions.kafka.KafkaProducer.Conf
import org.apache.kafka.common.serialization.StringSerializer
import com.google.inject.{Inject, Singleton}

@Singleton
class Kafka @Inject() (configuration: play.api.Configuration) {

  val producer = KafkaProducer(
    Conf(new StringSerializer(), new StringSerializer(), bootstrapServers = configuration.getString("kafka.endpoint").get)
  )

  // Subscribe to Kafka topics on initialization
  subscribeToKafkaTopics()

  def subscribeToKafkaTopics() = {
    // TODO: Add subscription to Kafka topics here to read processed images, which we will then push to actors
    println("Here go Kafka topic subscriptions")
  }

  def send(value: String) = producer.send(KafkaProducerRecord(topic="Images.Urls", key=None, value))
    .foreach(record => s"Pushed record to kafka: ${record.toString}")

  // Test producer
  send("http://www.some.com")

}

object Kafka {
//  var producer: KafkaProducer[String, String]
//
//  def setProducer(producer: KafkaProducer[String, String]) = {
//    producer = producer
//  }
//
//  def send(value: String) = producer.send(KafkaProducerRecord(topic="Images.Urls", key=None, value))
//    .foreach(record => s"Pushed record to kafka: ${record.toString}")

  var kafkaInstance: Kafka

  def setInstance(instance: Kafka) = {
    kafkaInstance = instance
  }

  def getInstance() = kafkaInstance
}
