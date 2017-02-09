package DataTypes

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData.Record

case class ImageRequest2(url: String, filter: String) extends AvroRecordConvertible {
  def toAvroRecord(avroSchema: Schema): Record = {
    val avroRecord = new Record(avroSchema)
    avroRecord.put("url", url)
    avroRecord.put("filter", filter)
    avroRecord
  }
}
