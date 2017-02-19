package DataTypes

import org.apache.avro.generic.GenericData.EnumSymbol
import org.apache.avro.generic.{GenericRecord, IndexedRecord}
import org.apache.avro.util.Utf8

import java.nio.ByteBuffer
import java.util.Base64

case class ImageProcessed(content: Array[Byte], id: String, format: String) {
  def contentAsBase64EncodedString(): String = new String(Base64.getEncoder.encode(content))
}

object ImageProcessed {
  def apply(record: GenericRecord): ImageProcessed =
    new ImageProcessed(
      content = record.get("content").asInstanceOf[ByteBuffer].array(),
      id = record.get("id").asInstanceOf[Utf8].toString,
      format = record.get("format").asInstanceOf[EnumSymbol].toString
    )
}

