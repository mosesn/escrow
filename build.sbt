organization := "com.mosesn"

name := "escrow"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.twitter" %% "util-core" % "6.12.1",
  "com.google.guava" % "guava" % "16.0.1",
  "com.google.code.findbugs" % "jsr305" % "2.0.2"
)

scalacOptions += "-language:implicitConversions"
