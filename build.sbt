name := "Eve"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.mongodb" %% "casbah" % "2.7.3",
  "com.google.code.gson" % "gson" % "2.4",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)