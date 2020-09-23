name := "ScalaTerraform"
version := "0.2-SNAPSHOT"
organization := "com.syedatifakhtar"
resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"
assemblyJarName in assembly := "scalaterraform.jar"
scalaVersion := "2.12.11"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest-funsuite" % "3.2.0" % "test",
  "org.scalactic" % "scalactic_2.12" % "3.2.0",
  "org.scalatest" %% "scalatest-funspec" % "3.2.0" % "test",
  "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.0" % "test",
  "org.scalatest" %% "scalatest-mustmatchers" % "3.2.0" % "test",
  "commons-io" % "commons-io" % "2.8.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.play" %% "play-json" % "2.8.1"
)
