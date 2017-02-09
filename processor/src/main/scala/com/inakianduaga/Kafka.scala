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
import akka.kafka._
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
//    urlToBinaryProcessorUsingStreams
    runImageProcessor
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

    Consumer.committableSource(consumerSettings, Subscriptions.topics("Images.Urls"))
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

  def runImageProcessor = {
    import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
    import io.confluent.kafka.serializers.{ KafkaAvroSerializer, KafkaAvroDeserializer, KafkaAvroDeserializerConfig }
    import org.apache.avro.Schema
    import java.io.File
    import org.apache.avro.generic.IndexedRecord
    import com.inakianduaga.models.ImageRequest2

    val config = ConfigFactory.load()
    implicit val system = ActorSystem.create("kafka-image-processor", config)
    implicit val mat = ActorMaterializer()

    // Empty Record from Avro schema
    val schemaFile = new File(getClass.getClassLoader.getResource("schemas/imageRequest2.avsc").getPath)
    val schema: Schema = new Schema.Parser().parse(schemaFile)

    // Avro serializer (uses schema)
    val schemaRegistryEndpoint = s"http://${Properties.envOrElse("SCHEMA_REGISTRY_ENDPOINT", "localhost:8081")}"
    val schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryEndpoint,1000)
    val avroSerializer = new KafkaAvroSerializer(schemaRegistryClient)

    val avroDeserializer = new KafkaAvroDeserializer(schemaRegistryClient)

    val consumerSettings = ConsumerSettings(system, avroDeserializer, avroDeserializer)
      .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
      .withGroupId("kafka-image-binary-processor")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

    Consumer.committableSource(consumerSettings, Subscriptions.topics("Images.Urls"))
      .map(message => message.committableOffset -> ImageRequest2(message.record.value.asInstanceOf[IndexedRecord]))
//      .mapAsync(1)(message => httpGet(message._2.url).map(response => (message._1, response)))
//      .filter(response => response._2.status == 200)
//      .map(response => (response._1, response._2.bodyAsBytes))
//      .map(imgBytes => (imgBytes._1, new ByteArrayInputStream(imgBytes._2)))
//      .map(imgInputStream => (imgInputStream._1, ImgLib.Image.fromStream(imgInputStream._2)))
//      .map(image => (image._1, image._2.filter(ImgLib.filter.GrayscaleFilter)))
//      .map(image => (image._1, new ProducerRecord[String, String]("Images.Filtered", image._2.bytes.toString)))
//      .map { pair => ProducerMessage.Message(pair._2, pair._1)}
//      .runWith(Producer.commitableSink(producerSettings))
      .map(data => println(s"Producer: Reading record with url: ${data._2.url} & filter value: ${data._2.filter}"))
      .runWith(Sink.ignore)


  }

}


