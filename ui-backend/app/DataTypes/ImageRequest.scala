package DataTypes

import org.apache.avro.generic.GenericData.Record
import org.apache.avro.Schema

case class ImageRequest(url: String) extends AvroRecordConvertible {

  def toAvroRecord(avroSchema: Schema): Record = {
    val avroRecord = new Record(avroSchema)
    avroRecord.put("url", url)
    avroRecord
  }

}
