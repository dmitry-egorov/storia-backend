package com.dmitryegorov.tools.serialization

import org.joda.time.DateTime
import org.json4s.CustomSerializer
import org.json4s.JsonAST.JInt

class DateTimeSerializer extends CustomSerializer[DateTime](format => (
    {
        case JInt(value) => new DateTime(value.longValue())
    },
    {
        case x: DateTime => JInt(x.toInstant.getMillis)
    }
    ))
