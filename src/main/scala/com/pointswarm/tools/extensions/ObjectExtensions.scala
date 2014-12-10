package com.pointswarm.tools.extensions

import scala.collection.JavaConversions._

object ObjectExtensions
{

    implicit class AnyEx(cc: Any)
    {
        def toJava: AnyRef =
        {
            cc match
            {
                case l: List[_]   => l.map(i => i.toJava).toArray
                case m: Map[_, _] => mapAsJavaMap(m.map(x => x._1 -> x._2.toJava).toMap)
                case null         => null
                case x: AnyRef    => x
            }
        }

        def toScala: AnyRef =
        {
            cc match
            {
                case l: Array[_]                => l.map(i => i.toScala).toList
                case m: java.util.HashMap[_, _] => m.map(x => x._1 -> x._2.toScala).toMap
                case null                       => null
                case x: AnyRef                  => x
            }
        }
    }

}
