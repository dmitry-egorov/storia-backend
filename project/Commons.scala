import sbt.Keys._
import sbt._

object Commons
{
    val settings: Seq[Def.Setting[_]] = Seq(
        scalaVersion := "2.11.4",
        scalacOptions := Seq("-feature", "-deprecation", "-unchecked", "-language:postfixOps", "-language:implicitConversions", "-language:existentials"),
        resolvers += "Element Releases" at "http://repo.element.hr/nexus/content/repositories/releases/"
    )
}