package com.inakianduaga.deserializers

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import org.apache.avro.Schema

trait ImageRequestDeserializer extends KafkaAvroDeserializer {

  var readerSchema: Option[Schema] = None

  def setReaderSchema(schema: Schema): this.type = {
    readerSchema = Some(schema)
    this
  }

  override def deserialize(s: String, bytes: Array[Byte]): Object =
    if (readerSchema.isEmpty)
      super.deserialize(s, bytes)
    else
      super.deserialize(s, bytes, readerSchema.get)
}

object ImageRequestDeserializer {
  def apply(client: SchemaRegistryClient) = new KafkaAvroDeserializer(client) with ImageRequestDeserializer
}
