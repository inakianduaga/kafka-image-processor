name := """kafka-image-processor"""

version := "0.1"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// Scala kafka client
resolvers += Resolver.bintrayRepo("cakesolutions", "maven")
libraryDependencies += "net.cakesolutions" %% "scala-kafka-client" % "0.10.0.0"

mainClass in Compile := Some("com.inakianduaga.Main")
