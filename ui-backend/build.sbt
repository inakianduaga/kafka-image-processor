name := """kafka-image-processor-ui-backend"""

version := "0.1"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

// https://github.com/sbt/sbt/issues/2054
//resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)

// Scala kafka client
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"

// Avro serializer/deserializer
resolvers += "confluent" at "http://packages.confluent.io/maven/"
libraryDependencies += "io.confluent" % "kafka-avro-serializer" % "3.1.2"

// Add AvroHugger tasks
//libraryDependencies += "com.julianpeeters" %% "avrohugger-core" % "0.13.0"
//sbtavrohugger.SbtAvrohugger.avroSettings

fork in run := false
