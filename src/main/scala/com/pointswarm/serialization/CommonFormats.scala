package com.pointswarm.serialization

import com.pointswarm.common.EventId
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization

object CommonFormats
{
    implicit val formats = Serialization.formats(NoTypeHints) +
                           new SingleValueCaseClassSerializer[EventId](x => new EventId(x))
}
