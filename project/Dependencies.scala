import sbt._

object Dependencies
{
    val scalatest = "org.scalatest" %% "scalatest" % "2.2.1" % "test"

    val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.0"
    val json4s = "org.json4s" %% "json4s-jackson" % "3.2.11"
    val json4s_ext = "org.json4s" %% "json4s-ext" % "3.2.11"
    val rx = "io.reactivex" %% "rxscala" % "0.22.0"
    val firebase = "com.firebase" % "firebase-client-jvm" % "2.0.2"
    val dispatcher = "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"
    val uri = "com.netaporter" %% "scala-uri" % "0.4.3"
    val scalatime = "com.github.nscala-time" %% "nscala-time" % "1.6.0"
    val transliteration = "hr.element.etb" %% "scala-transliteration" % "0.0.1"

    val toolsDependencies: Seq[ModuleID] = Seq(scalatest, json4s, rx, dispatcher, uri, scalatime, transliteration)
    val hellfireDependencies: Seq[ModuleID] = Seq(json4s, firebase, rx)
    val scalasourcingDependencies: Seq[ModuleID] = Seq(scalatest)
    val storiaWorkerDependencies: Seq[ModuleID] = Seq(scalatest, scalaz, json4s, json4s_ext, rx, firebase, scalatime)
}