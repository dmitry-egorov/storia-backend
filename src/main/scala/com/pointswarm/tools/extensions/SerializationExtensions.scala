package com.pointswarm.tools.extensions

import org.json4s.Formats
import org.json4s.jackson.Serialization

object SerializationExtensions
{
    implicit class StringEx(val s: String) extends AnyVal
    {
        def readAs[T](implicit m: Manifest[T], f: Formats) =
        {
            Serialization.read[T](s)
        }
    }

    implicit class AnyRefEx(val a: AnyRef) extends AnyVal
    {
        def toJson(implicit f: Formats) =
        {
            Serialization.write(a)
        }
    }
}
