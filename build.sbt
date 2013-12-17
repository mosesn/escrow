organization := "com.mosesn"

name := "escrow"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-core" % "6.10.0",
  "com.twitter" %% "util-core" % "6.10.0"
)

scalacOptions += "-language:implicitConversions"
