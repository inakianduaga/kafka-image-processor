package com.inakianduaga

import org.apache.kafka.clients.consumer.KafkaConsumer
import java.util.{ Properties => JavaProperties}
import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
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

  val consumer = {
    val props = new JavaProperties()
    props.put("bootstrap.servers", Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
    props.put("group.id", "kafka-image-processor-consumer-group")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

    new KafkaConsumer[String, String](props)
  }

  val producer = KafkaProducer(
    Conf(new StringSerializer(), new StringSerializer(), bootstrapServers = Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
  )

  println("Setting up kafka bindings")

  println(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
  
  // Subscribe to urls topic
  consumer.subscribe(List("Images.Urls").asJava)

  // Fetch records continuously
  while(true) {
    val records = consumer.poll(1000).asScala
    records.foreach(record => println("Received message: (" + record.key() + ", " + record.value() + ") at offset " + record.offset()))
  }

  // Keep main thread alive indefinitely
  val keepAlive = new CountDownLatch(1)
  keepAlive.await()

}


