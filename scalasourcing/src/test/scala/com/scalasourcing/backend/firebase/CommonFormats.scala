package com.scalasourcing.backend.firebase

import com.scalasourcing.backend.Root
import com.scalasourcing.backend.Root.{RootCommand, RootEvent}
import com.scalasourcing.backend.serialization.SingleStringCaseClassSerializer
import org.json4s._
import com.scalasourcing.backend.serialization.SingleStringCaseClassSerializer._

object CommonFormats
{
    def formats: Formats = new Formats
    {
        val dateFormat = DefaultFormats.lossless.dateFormat
        override val typeHints = ShortTypeHints(List(classOf[RootEvent], classOf[RootCommand]))
        override val typeHintFieldName = "t"
    } + SingleStringCaseClassSerializer[Root.Id]()
}
