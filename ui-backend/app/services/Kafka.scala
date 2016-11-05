package services

import com.google.inject.Singleton

@Singleton
class Kafka() {

  // Subscribe to Kafka topics on initialization
  subscribeToKafkaTopics

  def subscribeToKafkaTopics = {
    // TODO: Add subscription to Kafka topics here, which we will then push to actors
    println("Here go Kafka topic subscriptions")
  }

}
