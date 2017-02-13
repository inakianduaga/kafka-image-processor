package com.inakianduaga


//import org.apache.kafka.clients.consumer.KafkaConsumer
//import java.util.{Properties => JavaProperties}
import java.io.ByteArrayInputStream

import akka.actor.ActorSystem
import akka.kafka._
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import com.inakianduaga.deserializers.ImageRequestDeserializer
import com.inakianduaga.models.{ImageProcessed, ImageRequest2}
import com.inakianduaga.services.HttpClient.{get => httpGet}
import com.sksamuel.{scrimage => ImgLib}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Properties

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
    import java.io.File

    import com.inakianduaga.models.ImageRequest2
    import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
    import io.confluent.kafka.serializers.KafkaAvroSerializer
    import org.apache.avro.Schema
    import org.apache.avro.generic.IndexedRecord

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
      .map(message => ImageRecordData(message.committableOffset, ImageRequest2(message.record.value.asInstanceOf[IndexedRecord]), None))
      .mapAsync(1)(data =>
        httpGet(data.imageRequest.url).map(response => data.copy(holder = response))
      )
      .filter(data => data.holder.status == 200)
      .map(data => {
        val stream = new ByteArrayInputStream(data.holder.bodyAsBytes)
        val img = ImgLib.Image.fromStream(stream)
        val filter = data.imageRequest.filter.filter
        val imageProcessed = ImageProcessed(img.filter(filter).bytes, data.imageRequest.id ,Some("JPEG"))
        val producerRecord = new ProducerRecord[Object, Object]("Images.Processed", imageProcessed.toAvroRecord(writerSchema))
        ProducerMessage.Message(producerRecord, data.commitableOffset)
      })
      .runWith(Producer.commitableSink(producerSettings))
  }

  case class ImageRecordData[T](commitableOffset: ConsumerMessage.CommittableOffset, imageRequest: ImageRequest2,  holder: T)

}


