package com.scalasourcing.backend.firebase

import com.scalasourcing.backend.Root.RootEvent
import org.json4s._

object CommonFormats
{
    def formats: Formats = new Formats
    {
        val dateFormat = DefaultFormats.lossless.dateFormat
        override val typeHints = ShortTypeHints(List(classOf[RootEvent]))
        override val typeHintFieldName = "type"
    }
}
