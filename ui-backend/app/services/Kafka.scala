package services

import DataTypes.AvroRecordConvertible
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.google.inject.{Inject, Singleton}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import play.Environment

import scala.concurrent.Future
import scala.util.Properties

@Singleton
class Kafka @Inject() (configuration: play.api.Configuration, environment: Environment) {

  implicit val system: ActorSystem = ActorSystem.create("kafka-image-processor", ConfigFactory.load())
  implicit val mat: ActorMaterializer = ActorMaterializer()

  def subscribeToProcessedImages() = {

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
      .withGroupId("kafka-processed-image-reader")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

    // TODO: Add subscription to Kafka topics here to read processed images, which we will then push to actors
    Consumer.committableSource(consumerSettings, Subscriptions.topics("Images.Filtered"))
      .map(message => println(s"Received filtered message w/ value ${message.record.value()}"))
      .runWith(Sink.ignore)
  }

  def send(value: String): Future[Done] = {
    val producerSettings = ProducerSettings(system, new StringSerializer(), new StringSerializer())
      .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))

    Source.single(new ProducerRecord[String, String]("Images.Urls", value))
      .runWith(Producer.plainSink(producerSettings))
  }

  def send(recordData: AvroRecordConvertible): Future[Done] = {
    import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
    import io.confluent.kafka.serializers.KafkaAvroSerializer
    import org.apache.avro.Schema

    // Empty Record from Avro schema
    val schemaFile = environment.getFile("./app/schemas/imageRequest.avsc")
    val schema: Schema = new Schema.Parser().parse(schemaFile)

    // Avro serializer (uses schema)
    val schemaRegistryEndpoint = s"http://${Properties.envOrElse("SCHEMA_REGISTRY_ENDPOINT", "localhost:8081")}"
    val schemaRegistryClient = new CachedSchemaRegistryClient(schemaRegistryEndpoint,1000)
    val avroSerializer = new KafkaAvroSerializer(schemaRegistryClient)

    // kafka producer settings that uses with Akka Stream Kafka
    val producerSettings =
      ProducerSettings(system, avroSerializer, avroSerializer)
        .withBootstrapServers(Properties.envOrElse("KAFKA_ENDPOINT", "localhost:9092"))
        .withProperty("schema.registry.url", schemaRegistryEndpoint)

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
