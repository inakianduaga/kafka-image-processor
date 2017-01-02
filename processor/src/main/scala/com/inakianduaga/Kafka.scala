package com.inakianduaga

import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord, KafkaConsumer}
import cakesolutions.kafka.KafkaProducer.Conf
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import scala.util.Properties
import scala.collection.JavaConverters._
import scala.concurrent.duration._
import java.util.concurrent.CountDownLatch

class Kafka {

}

object Kafka {

  def main(args:Array[String]): Unit = {
  }

  val producer = KafkaProducer(
    Conf(new StringSerializer(), new StringSerializer(), bootstrapServers = Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
  )

  val consumer = KafkaConsumer(
    KafkaConsumer.Conf(
      new StringDeserializer(),
      new StringDeserializer(),
      bootstrapServers = Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"),
      "kafka-image-processor-consumer-group"
    )
  )

  println("Setting up kafka bindings")

  println(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
  
  // Subscribe to urls topic
  consumer.subscribe(List("Images.Urls").asJava)

  // Fetch records continuously
  while(true) {
    val records = consumer.poll(2000).asScala
    println("fetching...")
    print(s"number: ${records.size}")
    records.foreach(record => println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset()))
  }

  // Keep main thread alive indefinitely
  val keepAlive = new CountDownLatch(1)
  keepAlive.await()
}
