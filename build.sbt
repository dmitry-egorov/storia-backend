enablePlugins(JavaAppPackaging)

name := """storia-worker"""

version := "1.0"

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-feature", "-deprecation", "-language:postfixOps", "-language:implicitConversions")

resolvers += "Element Releases" at "http://repo.element.hr/nexus/content/repositories/releases/"

libraryDependencies ++= Seq(
  "com.firebase" % "firebase-client-jvm" % "2.0.2",
  "io.reactivex" %% "rxscala" % "0.22.0",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "com.netaporter" %% "scala-uri" % "0.4.3",
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
  "com.github.nscala-time" %% "nscala-time" % "1.6.0",
  "hr.element.etb" %% "scala-transliteration" % "0.0.1"
)