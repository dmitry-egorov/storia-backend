enablePlugins(JavaAppPackaging)

name := """storia-worker"""

version := "1.0"

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= Seq(
  "com.firebase" % "firebase-client-jvm" % "2.0.2",
  "io.reactivex" %% "rxscala" % "0.22.0",
  "org.json4s" %% "json4s-jackson" % "3.2.11",
  "org.scala-lang.modules" %% "scala-async" % "0.9.2",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.2",
  "com.netaporter" %% "scala-uri" % "0.4.3"
)

resolvers += "gphat" at "https://raw.github.com/gphat/mvn-repo/master/releases/"