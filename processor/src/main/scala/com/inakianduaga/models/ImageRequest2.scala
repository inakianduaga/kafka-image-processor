package com.inakianduaga.models

import com.sksamuel.{scrimage => ImgLib}
import org.apache.avro.generic.GenericData.EnumSymbol
import org.apache.avro.generic.GenericRecord
import org.apache.avro.util.Utf8
import scala.concurrent.ExecutionContext.Implicits.global

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
        case BlurFilter.name => BlurFilter
        case DiffuseFilter.name => DiffuseFilter
        case ContourFilter.name => ContourFilter
        case EdgeFilter.name => EdgeFilter
        case OilFilter.name => OilFilter
        case RobertsFilter.name => RobertsFilter
        case SummerFilter.name => SummerFilter
        case TritoneFilter.name => TritoneFilter
        case SolarizeFilter.name => SolarizeFilter
        case TelevisionFilter.name => TelevisionFilter
        case VintageFilter.name => VintageFilter
        case InvertFilter.name => InvertFilter
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

case object BlurFilter extends Filter {
  val name = "BLUR"
  override val filter = ImgLib.filter.BlurFilter
}

case object DiffuseFilter extends Filter {
  val name = "DIFFUSE"
  override val filter = ImgLib.filter.DiffuseFilter(3)
}

case object ContourFilter extends Filter {
  val name = "CONTOUR"
  override val filter = ImgLib.filter.ContourFilter()
}

case object EdgeFilter extends Filter {
  val name = "EDGE"
  override val filter = ImgLib.filter.EdgeFilter
}

case object OilFilter extends Filter {
  val name = "OIL"
  override val filter = ImgLib.filter.OilFilter(2, 4)
}

case object RobertsFilter extends Filter {
  val name = "ROBERTS"
  override val filter = ImgLib.filter.RobertsFilter
}

case object SummerFilter extends Filter {
  val name = "SUMMER"
  override val filter = ImgLib.filter.SummerFilter(true)
}

case object TritoneFilter extends Filter {
  val name = "TRITONE"
  override val filter = ImgLib.filter.TritoneFilter(150, 80, 50)
}

case object SolarizeFilter extends Filter {
  val name = "SOLARIZE"
  override val filter = ImgLib.filter.SolarizeFilter
}

case object TelevisionFilter extends Filter {
  val name = "TELEVISION"
  override val filter = ImgLib.filter.TelevisionFilter
}

case object VintageFilter extends Filter {
  val name = "VINTAGE"
  override val filter = ImgLib.filter.VignetteFilter()
}

case object InvertFilter extends Filter {
  val name = "INVERT"
  override val filter = ImgLib.filter.InvertFilter
}







