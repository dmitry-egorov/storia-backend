package com.pointswarm.serialization

import org.json4s.CustomSerializer
import org.json4s.JsonAST._

class SingleValueCaseClassSerializer[T <: Product](constructor: String => T)(implicit m: Manifest[T])
    extends CustomSerializer[T](format => (
    {
        case JString(value) => constructor(value)
    },
    {
        case x: T => JString(x.productIterator.next().toString)
    }
    ))
