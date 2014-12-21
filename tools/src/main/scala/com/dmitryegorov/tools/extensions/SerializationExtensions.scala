package com.dmitryegorov.tools.extensions

import org.json4s.{Extraction, Formats}
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

    implicit class RichAny(val a: Any) extends AnyVal
    {
        def toJValue(implicit f: Formats) = Extraction.decompose(a)
    }

    implicit class AnyRefEx(val a: AnyRef) extends AnyVal
    {
        def toJson(implicit f: Formats) =
        {
            Serialization.write(a)
        }
    }
}
