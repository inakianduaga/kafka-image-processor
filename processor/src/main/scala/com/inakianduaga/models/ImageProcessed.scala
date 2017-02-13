package com.inakianduaga.models

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData.Record

case class ImageProcessed(content: Array[Byte], id: String, format: Option[String]) {
  def toAvroRecord(avroSchema: Schema): Record = {
    val avroRecord = new Record(avroSchema)
    avroRecord.put("content", content)
    avroRecord.put("format", format.orNull)
    avroRecord
  }
}
