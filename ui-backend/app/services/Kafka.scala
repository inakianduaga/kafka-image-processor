package services

import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import cakesolutions.kafka.KafkaProducer.Conf
import org.apache.kafka.common.serialization.StringSerializer

import com.google.inject.{Singleton, Inject}

@Singleton
class Kafka @Inject() (configuration: play.api.Configuration) {

  val producer = KafkaProducer(
    Conf(new StringSerializer(), new StringSerializer(), bootstrapServers = configuration.getString("kafka.endpoint").get)
  )

  // Subscribe to Kafka topics on initialization
  subscribeToKafkaTopics

  def subscribeToKafkaTopics = {
    // TODO: Add subscription to Kafka topics here, which we will then push to actors
    println("Here go Kafka topic subscriptions")
  }

}
