package com.inakianduaga.models

import com.sksamuel.{scrimage => ImgLib}
import org.apache.avro.generic.GenericData.EnumSymbol
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8

case class ImageRequest2(url: String, id: String, filter: Filter) {}

object ImageRequest2 {

  def apply(record: GenericRecord): ImageRequest2 =
    ImageRequest2(
      url = record.get("url").asInstanceOf[Utf8].toString,
      id = record.get("id").asInstanceOf[Utf8].toString,
      filter = record.get("filter").asInstanceOf[EnumSymbol].toString match {
        case FilterGreyscale.name => FilterGreyscale
        case FilterChrome.name => FilterChrome
        case FilterHalftone.name => FilterHalftone
      }
    )

}

sealed trait Filter {
  val name: String
  val filter: ImgLib.Filter
}
case object FilterGreyscale extends Filter {
  val name = "GREYSCALE"
  override val filter = ImgLib.filter.GrayscaleFilter
}
case object FilterChrome extends Filter {
  val name = "CHROME"
  override val filter = ImgLib.filter.ChromeFilter()
}

case object FilterHalftone extends Filter {
  val name = "HALFTONE"
  override val filter = ImgLib.filter.ColorHalftoneFilter()
}






