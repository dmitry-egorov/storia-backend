package com.pointswarm.tools.extensions

import org.json4s.Formats
import org.json4s.jackson.Serialization

object SerializationExtensions
{
    implicit class StringEx(s: String)
    {
        def readAs[T](implicit m: Manifest[T], f: Formats) = Serialization.read[T](s)
    }

    implicit class AnyRefEx(a: AnyRef)
    {
        def toJson(implicit f: Formats) = Serialization.write(a)
    }
}
