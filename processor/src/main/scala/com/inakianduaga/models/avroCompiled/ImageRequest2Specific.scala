/** MACHINE-GENERATED FROM AVRO SCHEMA. DO NOT EDIT DIRECTLY */
package com.inakianduaga.models.avroCompiled

import scala.annotation.switch

/**
 * Image processing request
 * @param url The image location
 * @param id The id of this processing request
 * @param filter The filter to apply to this image
 */
case class ImageRequest2Specific(var url: String, var id: String, var filter: filters = filters.GREYSCALE) extends org.apache.avro.specific.SpecificRecordBase {
  def this() = this("", "", filters.GREYSCALE)
  def get(field$: Int): AnyRef = {
    (field$: @switch) match {
      case pos if pos == 0 => {
        url
      }.asInstanceOf[AnyRef]
      case pos if pos == 1 => {
        id
      }.asInstanceOf[AnyRef]
      case pos if pos == 2 => {
        filter
      }.asInstanceOf[AnyRef]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
  }
  def put(field$: Int, value: Any): Unit = {
    (field$: @switch) match {
      case pos if pos == 0 => this.url = {
        value.toString
      }.asInstanceOf[String]
      case pos if pos == 1 => this.id = {
        value.toString
      }.asInstanceOf[String]
      case pos if pos == 2 => this.filter = {
        value
      }.asInstanceOf[filters]
      case _ => new org.apache.avro.AvroRuntimeException("Bad index")
    }
    ()
  }
  def getSchema: org.apache.avro.Schema = ImageRequest2Specific.SCHEMA$
}

object ImageRequest2Specific {
  val SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ImageRequest\",\"namespace\":\"inakianduaga.kafkaImageProcessor\",\"doc\":\"Image processing request\",\"fields\":[{\"name\":\"url\",\"type\":\"string\",\"doc\":\"The image location\",\"aliases\":[\"downloadUrl\"]},{\"name\":\"id\",\"type\":\"string\",\"doc\":\"The id of this processing request\"},{\"name\":\"filter\",\"type\":{\"type\":\"enum\",\"name\":\"filters\",\"symbols\":[\"GREYSCALE\",\"CHROME\",\"HALFTONE\",\"BLUR\",\"DIFFUSE\",\"CONTOUR\",\"EDGE\",\"OIL\",\"ROBERTS\",\"SUMMER\",\"TRITONE\",\"SOLARIZE\",\"TELEVISION\",\"VINTAGE\",\"INVERT\"]},\"doc\":\"The filter to apply to this image\",\"default\":\"GREYSCALE\"}]}")
}
