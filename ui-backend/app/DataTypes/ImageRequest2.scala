package DataTypes

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData.Record

case class ImageRequest2(url: String, filter: FilterName) extends AvroRecordConvertible {
  def toAvroRecord(avroSchema: Schema): Record = {
    val avroRecord = new Record(avroSchema)
    avroRecord.put("url", url)
    avroRecord.put("filter", filter.name)
    avroRecord
  }
}

sealed trait FilterName {
  val name: String
}
case object FilterGreyscale extends FilterName {
  val name = "GREYSCALE"
}
case object FilterInverted extends FilterName {
  val name = "INVERTED"
}

case object FilterRainbow extends FilterName {
  val name = "RAINBOW"
}
