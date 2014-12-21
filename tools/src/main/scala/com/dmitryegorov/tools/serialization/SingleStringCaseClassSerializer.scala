package com.dmitryegorov.tools.serialization

import org.json4s._

object SingleStringCaseClassSerializer
{

    implicit class FormatsEx(val f: Formats) extends AnyVal
    {
        def +[T](t: (Serializer[T], KeySerializer[T])) = f + t._1 + t._2
    }

    def apply[T <: Product](constructor: String => T)(implicit m: Manifest[T]): (Serializer[T], KeySerializer[T]) = (new Value[T](constructor), new Key[T](constructor))

    class Value[T <: Product](constructor: String => T)(implicit m: Manifest[T])
        extends CustomSerializer[T](format => (
            {
                case JString(value) => constructor(value)
            },
            {
                case x: T => JString(x.productIterator.next().toString)
            }
            ))

    class Key[T <: Product](constructor: String => T)(implicit m: Manifest[T])
        extends CustomKeySerializer[T](format => (
            {
                case value => constructor(value)
            },
            {
                case x: T => x.productIterator.next().toString
            }
            ))

}
