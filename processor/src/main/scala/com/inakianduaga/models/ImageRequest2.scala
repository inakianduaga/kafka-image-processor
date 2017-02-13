package com.inakianduaga.models

import com.sksamuel.{scrimage => ImgLib}
import org.apache.avro.generic.IndexedRecord

case class ImageRequest2(url: String, id: String, filter: Filter) {}

object ImageRequest2 {
  def apply(record: IndexedRecord): ImageRequest2 =
    ImageRequest2(
      url = record.get(record.getSchema.getField("downloadUrl").pos()).asInstanceOf[String],
      id = record.get(record.getSchema.getField("id").pos()).asInstanceOf[String],
      filter = record.get(record.getSchema.getField("filter").pos()).asInstanceOf[String] match {
        case "GREYSCALE" => FilterGreyscale
        case "CHROME" => FilterChrome
        case "HALFTONE" => FilterHalftone
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






