package com.inakianduaga

import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import cakesolutions.kafka.KafkaProducer.Conf
import org.apache.kafka.common.serialization.StringSerializer
import scala.util.Properties

class Processor {

}

object Processor {

  val producer = KafkaProducer(
    Conf(new StringSerializer(), new StringSerializer(), bootstrapServers = Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
  )



  // def send(record: ProducerRecord[K, V]): Future[RecordMetadata]
  // def sendWithCallback(record: ProducerRecord[K, V])(callback: Try[RecordMetadata] => Unit): Unit
}
