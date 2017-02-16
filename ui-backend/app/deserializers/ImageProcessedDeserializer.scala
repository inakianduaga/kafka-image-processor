package deserializers

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.Schema

//class ImageProcessedDeserializer extends KafkaAvroDeserializer {
//
//  var readerSchema: Option[Schema] = None
//
//  def setSchemaRegistryClient(client: SchemaRegistryClient) {
//    this.schemaRegistry = client
//  }
//
//  def setReaderSchema(schema: Schema): this.type = {
//    readerSchema = Some(schema)
//    this
//  }
//
//  override def deserialize(s: String, bytes: Array[Byte]): Object =
//    if (readerSchema.isEmpty)
//      super.deserialize(s, bytes)
//    else
//      super.deserialize(s, bytes, readerSchema.get)
//}
//
//object ImageProcessedDeserializer {
//  def apply(client: SchemaRegistryClient) = {
//    val deserializer = new ImageProcessedDeserializer()
//    deserializer.setSchemaRegistryClient(client)
//    deserializer
//  }
//
//  def apply(client: SchemaRegistryClient, readerSchema: Schema): ImageProcessedDeserializer = ImageProcessedDeserializer(client).setReaderSchema(readerSchema)
//}
