name := """kafka-image-processor"""

version := "0.1"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// Scala kafka
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"

// Play HTTP standalone client
libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.4.3"

// Image processing
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.0"
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.0"
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.0"

mainClass in Compile := Some("com.inakianduaga.Kafka")

// Avro serializer/deserializer
resolvers += "confluent" at "http://packages.confluent.io/maven/"
libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "3.1.2"
