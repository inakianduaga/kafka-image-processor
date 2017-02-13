package DataTypes

import org.apache.avro.generic.IndexedRecord

case class ImageProcessed(content: Array[Byte], id: String, format: String)

object ImageProcessed {
  def apply(record: IndexedRecord): ImageProcessed =
    new ImageProcessed(
      content = record.get(record.getSchema.getField("content").pos()).asInstanceOf[Array[Byte]],
      id = record.get(record.getSchema.getField("id").pos()).asInstanceOf[String],
      format = record.get(record.getSchema.getField("format").pos()).asInstanceOf[String]
    )
}

