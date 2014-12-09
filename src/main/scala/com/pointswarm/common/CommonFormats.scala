package com.pointswarm.common

import com.pointswarm.tools.serialization.SingleValueCaseClassSerializer
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization

object CommonFormats
{
    implicit val formats = Serialization.formats(NoTypeHints) +
                           new SingleValueCaseClassSerializer[EventId](x => new EventId(x))
}
