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
import com.inakianduaga.deserializers.ImageRequestDeserializer
import com.inakianduaga.models.ImageProcessed
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

    // Aktor System
    val config = ConfigFactory.load()
    implicit val system = ActorSystem.create("kafka-image-processor", config)
    implicit val mat = ActorMaterializer()

    // Schema registry
    val schemaRegistryClient = {
      val schemaRegistryEndpoint = s"http://${Properties.envOrElse("SCHEMA_REGISTRY_ENDPOINT", "localhost:8081")}"
      new CachedSchemaRegistryClient(schemaRegistryEndpoint,1000)
    }

    // Consumer
    val readerSchema: Schema = {
      val readerSchemaFile = new File(getClass.getClassLoader.getResource("schemas/imageRequest2.avsc").getPath)
      new Schema.Parser().parse(readerSchemaFile)
    }
    val consumerSettings = {
      val avroDeserializer = new ImageRequestDeserializer(schemaRegistryClient).setReaderSchema(readerSchema)
      ConsumerSettings(system, avroDeserializer, avroDeserializer)
        .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
        .withGroupId("kafka-image-binary-processor")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    }

    // Producer
    val writerSchema: Schema = {
      val writerSchemaFile = new File(getClass.getClassLoader.getResource("schemas/imageProcessed.avsc").getPath)
      new Schema.Parser().parse(writerSchemaFile)
    }
    val producerSettings = {
      val avroSerializer = new KafkaAvroSerializer(schemaRegistryClient)
      ProducerSettings(system, avroSerializer, avroSerializer)
        .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
    }

    Consumer.committableSource(consumerSettings, Subscriptions.topics("Images.Urls"))
      .map(message => ImageRecordData(message.committableOffset, ImageRequest2(message.record.value.asInstanceOf[IndexedRecord])))
      .mapAsync(1)(imageRecordData =>
        httpGet(imageRecordData.imageTransformation.url)
          .map(response => ImageRecordData(imageRecordData.commitableOffset, response, Some(imageRecordData.imageTransformation.filter)))
      )
      .filter(data => data.imageTransformation.status == 200)
      .map(data => data.copy(imageTransformation = data.imageTransformation.bodyAsBytes))
      .map(data => data.copy(imageTransformation = new ByteArrayInputStream(data.imageTransformation)))
      .map(data => data.copy(imageTransformation = ImgLib.Image.fromStream(data.imageTransformation)))
      .map(data => data.copy(imageTransformation = data.imageTransformation.filter(data.filter.get.filter)))
      .map(data => data.copy(imageTransformation = ImageProcessed(data.imageTransformation.bytes, Some("JPEG"))))
      .map(data => data.copy(imageTransformation = new ProducerRecord[Object, Object]("Images.Filtered", data.imageTransformation.toAvroRecord(writerSchema))))
      .map(data => ProducerMessage.Message(data.imageTransformation, data.commitableOffset))
      .runWith(Producer.commitableSink(producerSettings))
  }

  case class ImageRecordData[T](commitableOffset: ConsumerMessage.CommittableOffset, imageTransformation: T, filter: Option[com.inakianduaga.models.Filter] = None)

}


