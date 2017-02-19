package com.inakianduaga.models

import com.sksamuel.{scrimage => ImgLib}
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8

case class ImageRequest2(url: String, id: String, filter: Filter) {}

object ImageRequest2 {

  def apply(record: GenericRecord): ImageRequest2 = {
    println("building imageRequest2")
    println(s"The Schema fields are ${record.getSchema.getFields.toString} - Length: ${record.getSchema.getFields.size()}")
    println(s"The url is: ${record.get("url").asInstanceOf[Utf8].toString}")
    println(s"The id is: ${record.get("id").asInstanceOf[Utf8].toString}")
    println(s"The filter is: ${record.get("filter").asInstanceOf[Utf8].toString}")
    ImageRequest2(
      url = record.get("url").asInstanceOf[Utf8].toString,
      id = record.get("id").asInstanceOf[Utf8].toString,
      filter = record.get("filter").asInstanceOf[Utf8].toString match {
        case "GREYSCALE" => FilterGreyscale
        case "CHROME" => FilterChrome
        case "HALFTONE" => FilterHalftone
      }
    )
  }

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






