package com.pointswarm.tools.extensions

import scala.collection.JavaConversions._

object ObjectExtensions
{

    implicit class AnyEx(cc: Any)
    {
        def toJava: Any =
        {
            cc match
            {
                case l: List[_]   => l.map(i => i.toJava).toArray
                case m: Map[_, _] => mapAsJavaMap(m.map(x => x._1 -> x._2.toJava).toMap)
                case null         => null
                case x: Any       => x
            }
        }
    }

}
