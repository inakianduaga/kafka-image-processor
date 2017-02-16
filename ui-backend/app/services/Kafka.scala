package services

import DataTypes.{AvroRecordConvertible, ImageProcessed}
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.ConfigFactory
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import org.apache.avro.Schema
import org.apache.avro.generic.IndexedRecord
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import play.Environment
import play.api.libs.json.Json
import scala.concurrent.Future
import scala.util.Properties

@Singleton
class Kafka @Inject() (configuration: play.api.Configuration, environment: Environment) {

  implicit val system: ActorSystem = ActorSystem.create("kafka-image-processor", ConfigFactory.load())
  implicit val mat: ActorMaterializer = ActorMaterializer()

  def subscribeToProcessedImages() = {

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
      val readerSchemaFile = environment.getFile("./app/schemas/imageProcessed.avsc")
      new Schema.Parser().parse(readerSchemaFile)
    }

    val consumerSettings = {
      val avroDeserializer = new KafkaAvroDeserializer(schemaRegistryClient)
      ConsumerSettings(system, avroDeserializer, avroDeserializer)
        .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
        .withGroupId("kafka-image-binary-processor")
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
    }

    Consumer.committableSource(consumerSettings, Subscriptions.topics("Images.Processed"))
      .map(message => ImageProcessed(message.record.value.asInstanceOf[IndexedRecord]))
      .runForeach(imageProcessed => WebSocketActor.push(imageProcessed.id, Json.obj("content" -> imageProcessed.content)))
  }

  def send(value: String): Future[Done] = {
    val producerSettings = ProducerSettings(system, new StringSerializer(), new StringSerializer())
      .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))

    Source.single(new ProducerRecord[String, String]("Images.Urls", value))
      .runWith(Producer.plainSink(producerSettings))
  }

  def send(recordData: AvroRecordConvertible, schemaPath: String): Future[Done] = {
    import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
    import io.confluent.kafka.serializers.KafkaAvroSerializer
    import org.apache.avro.Schema

    // Empty Record from Avro schema
    val schemaFile = environment.getFile(schemaPath)
    val schema: Schema = new Schema.Parser().parse(schemaFile)

    // Avro serializer (uses schema)
    val schemaRegistryEndpoint = s"http://${Properties.envOrElse("SCHEMA_REGISTRY_ENDPOINT", "localhost:8081")}"
    val schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryEndpoint,1000)
    val avroSerializer = new KafkaAvroSerializer(schemaRegistryClient)

    // kafka producer settings that uses with Akka Stream Kafka
    val producerSettings =
      ProducerSettings(system, avroSerializer, avroSerializer)
        .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))

    Source.single(new ProducerRecord[Object, Object]("Images.Urls", recordData.toAvroRecord(schema)))
      .runWith(Producer.plainSink(producerSettings))
  }

  // Subscribe to Kafka topics on initialization
  subscribeToProcessedImages()

  // HACK: Save instance into object companion for retrieving it without needing dependency injection
  Kafka.setInstance(this)


}

object Kafka {

  var kafkaInstance: Option[Kafka] = None

  def setInstance(instance: Kafka): Unit = {
    kafkaInstance = Some(instance)
  }

  def getInstance(): Kafka = kafkaInstance.get
}
