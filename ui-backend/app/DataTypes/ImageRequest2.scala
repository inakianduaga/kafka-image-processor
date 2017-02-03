package DataTypes

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData.Record

case class ImageRequest2(url: String, filter: Filter) extends AvroRecordConvertible {
  def toAvroRecord(avroSchema: Schema): Record = {
    val avroRecord = new Record(avroSchema)
    avroRecord.put("url", url)
    avroRecord.put("filter", filter.name)
    avroRecord
  }
}

sealed trait Filter {
  val name: String
}
case object FilterGreyscale extends Filter {
  val name = "GREYSCALE"
}
case object FilterChrome extends Filter {
  val name = "CHROME"
}

case object FilterHalftone extends Filter {
  val name = "HALFTONE"
}
