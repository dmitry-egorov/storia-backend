package com.dmitryegorov.tools.serialization

import org.json4s._

object SingleStringCaseClassSerializer
{
    implicit class FormatsEx(val f: Formats) extends AnyVal
    {
        def +[T](t: (Serializer[T], KeySerializer[T])) = f + t._1 + t._2
    }

    def apply[T <: Product : Manifest](): (Serializer[T], KeySerializer[T]) = (new Value[T](), new Key[T]())

    class Value[T <: Product : Manifest]
        extends CustomSerializer[T](format => (
            {
                case JString(value) => construct(value)
            },
            {
                case x: T => JString(x.productElement(0).toString)
            }
            ))

    class Key[T <: Product : Manifest]
        extends CustomKeySerializer[T](format => (
            {
                case value => construct(value)
            },
            {
                case x: T => x.productElement(0).toString
            }
            ))

    private def construct[T <: Product : Manifest](value: String): T =
    {
        implicitly[Manifest[T]].runtimeClass.getConstructors()(0).newInstance(value).asInstanceOf[T]
    }
}
