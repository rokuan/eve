name := "Eve"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.7.3",
  "com.google.code.gson" % "gson" % "2.4",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  //"com.typesafe.akka" %% "akka-actor" % "2.4.1",
  "org.apache.directory.api" % "api-all" % "1.0.0-M33",
  "com.jsuereth" % "scala-arm_2.11" % "2.0.0-M1",
  "org.scalaj" % "scalaj-http_2.11" % "2.3.0",
  "org.json4s" % "json4s-native_2.11" % "3.5.0",
  "org.json4s" % "json4s-jackson_2.11" % "3.5.0"
)