package com.inakianduaga.models

import java.nio.ByteBuffer
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData.Record

case class ImageProcessed(content: Array[Byte], id: String, format: Option[ImageFormat]) {
  def toAvroRecord(avroSchema: Schema): Record = {
    val avroRecord = new Record(avroSchema)
    avroRecord.put("content", ByteBuffer.wrap(content))
    avroRecord.put("format", format.map(_.format).orNull)
    avroRecord.put("id", id)
    avroRecord
  }
}

sealed trait ImageFormat {
  val format: String
}
case object Png extends ImageFormat {
  val format =  "PNG"
}
case object Jpg extends ImageFormat {
  val format =  "JPEG"
}
case object Gif extends ImageFormat {
  val format =  "GIF"
}
