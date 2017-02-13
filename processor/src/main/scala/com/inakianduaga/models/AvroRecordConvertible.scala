package com.inakianduaga.models

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData.Record

trait AvroRecordConvertible {
  /**
    * Note: Schema must be compatible with the data type that will generate the record
    *       otherwise exception will occur when pushing to Kafka
    */
  def toAvroRecord(avroSchema: Schema): Record
}
