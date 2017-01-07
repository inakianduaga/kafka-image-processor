name := """kafka-image-processor"""

version := "0.1"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// Scala kafka client
resolvers += Resolver.bintrayRepo("cakesolutions", "maven")
libraryDependencies += "net.cakesolutions" %% "scala-kafka-client" % "0.10.0.0"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "0.10.0.1"

// Play HTTP standalone client
libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.4.3"

// Image processing
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % "2.1.0"
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-io-extra" % "2.1.0"
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-filters" % "2.1.0"

mainClass in Compile := Some("com.inakianduaga.Kafka")

// set the main Scala source directory to be <base>/src
//scalaSource in Compile <<= baseDirectory(_ / "src")

// set the main class for the main 'run' task
// change Compile to Test to set it for 'test:run'
//mainClass in (Compile, run) := Some("com.inakianduaga.Kafka")
