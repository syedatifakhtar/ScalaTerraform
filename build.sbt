

lazy val root = (project in file("."))
  .settings(
    name := "ScalaTerraform",
    version := "0.8-SNAPSHOT",
    organization := "com.syedatifakhtar.scalaterraform",
    resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases",
    crossScalaVersions := Seq("2.13.4"),
    assemblyJarName in assembly := "scalaterraform.jar",
    scalaVersion := "2.13.4",
    homepage := Some(url("https://github.com/syedatifakhtar/ScalaTerraform")),
    scmInfo := Some(ScmInfo(url("https://github.com/syedatifakhtar/ScalaTerraform"), "git@github.com:syedatifakhtar/ScalaTerraform.git")),
    developers := List(Developer("atif", "atif", "syedatifakhtar@gmail.com", url("https://github.com/syedatifakhtar"))),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0")),
    publishMavenStyle := true
  )


libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest-funsuite" % "3.2.0" % "test",
  "org.scalactic" %% "scalactic" % "3.2.0",
  "org.scalatest" %% "scalatest-funspec" % "3.2.0" % "test",
  "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.0" % "test",
  "org.scalatest" %% "scalatest-mustmatchers" % "3.2.0" % "test",
  "commons-io" % "commons-io" % "2.8.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.play" %% "play-json" % "2.8.1"
)

publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)
