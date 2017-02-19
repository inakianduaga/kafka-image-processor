package com.inakianduaga

import java.io.{ByteArrayInputStream, File}

import akka.actor.ActorSystem
import akka.kafka._
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.ActorMaterializer
import com.inakianduaga.deserializers.ImageRequestDeserializer
import com.inakianduaga.models.{ImageProcessed, ImageRequest2}
import com.inakianduaga.services.HttpClient.{get => httpGet}
import com.sksamuel.{scrimage => ImgLib}
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Properties, Try}

object Kafka {

  def main(args:Array[String]): Unit = run()

  def run() =
    Consumer.committableSource(consumerSettings, Subscriptions.topics("Images.Urls"))
      .map(message => ImageRecordData(message.committableOffset, ImageRequest2(message.record.value.asInstanceOf[GenericRecord]), None))
      .mapAsync(1)(data =>
        httpGet(data.imageRequest.url).map(response => data.copy(container = response))
      )
      .filter(data => data.container.status == 200)
      .map(data => {
        println("downloaded image and now ready to process it further...")
        val stream = new ByteArrayInputStream(data.container.bodyAsBytes)
        val img = ImgLib.Image.fromStream(stream)
        val filter = data.imageRequest.filter.filter
        val imageProcessed = ImageProcessed(img.filter(filter).bytes, data.imageRequest.id ,Some("JPEG"))
        val producerRecord = new ProducerRecord[Object, Object]("Images.Processed", imageProcessed.toAvroRecord(writerSchema))
        ProducerMessage.Message(producerRecord, data.commitableOffset)
      })
      .runWith(Producer.commitableSink(producerSettings))
      .onFailure {
        case t: Throwable => s"There was an exception: ${t.getMessage}"
      }

  case class ImageRecordData[T](commitableOffset: ConsumerMessage.CommittableOffset, imageRequest: ImageRequest2, container: T)

  implicit val actorSystem = {
    val config = ConfigFactory.load()
    ActorSystem.create("kafka-image-processor", config)
  }
  implicit val actorMaterializer = ActorMaterializer()

  private val schemaRegistryClient = {
    val schemaRegistryEndpoint = s"http://${Properties.envOrElse("SCHEMA_REGISTRY_ENDPOINT", "localhost:8081")}"
    new CachedSchemaRegistryClient(schemaRegistryEndpoint,1000)
  }

  private val readerSchema: Schema = {
    val readerSchemaFile = new File(getClass.getClassLoader.getResource("schemas/imageRequest2.avsc").getPath)
    new Schema.Parser().parse(readerSchemaFile)
  }
  private val consumerSettings = {
    val avroDeserializer = ImageRequestDeserializer(schemaRegistryClient).setReaderSchema(readerSchema)
    ConsumerSettings(actorSystem, avroDeserializer, avroDeserializer)
      .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
      .withGroupId("kafka-image-binary-processor")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
  }

  private val writerSchema: Schema = {
    val writerSchemaFile = new File(getClass.getClassLoader.getResource("schemas/imageProcessed.avsc").getPath)
    new Schema.Parser().parse(writerSchemaFile)
  }
  private val producerSettings = {
    val avroSerializer = new KafkaAvroSerializer(schemaRegistryClient)
    ProducerSettings(actorSystem, avroSerializer, avroSerializer)
      .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
  }



}


