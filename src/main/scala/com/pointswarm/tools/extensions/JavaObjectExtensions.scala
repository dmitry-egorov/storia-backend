package com.pointswarm.tools.extensions

import scala.collection.JavaConversions._
import java.util

object JavaObjectExtensions
{

    implicit class JavaObjectEx(value: Any)
    {
        def javaToJson: String =
        {
            value match
            {
                case hm: util.HashMap[_, _] => "{" + hm.map(x => x._1.javaToJson + ":" + x._2.javaToJson).mkString(",") + "}"
                case s: String              => "\"" + s + "\""
                case i: Integer             => i.toString
                case l: util.List[_]        => "[" + l.map(_.javaToJson).mkString(",") + "]"
                case _                      => throw new UnsupportedOperationException
            }
        }
    }

}
