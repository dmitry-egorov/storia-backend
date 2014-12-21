package com.scalasourcing.backend.firebase

import com.scalasourcing.backend.TestRoot.{RootCommand, RootEvent}
import com.scalasourcing.backend.TestRootId
import com.scalasourcing.backend.firebase.SingleStringCaseClassSerializer._
import org.json4s.{DefaultFormats, Formats, ShortTypeHints}

object CommonFormats
{
    def formats: Formats = new Formats
    {
        val dateFormat = DefaultFormats.lossless.dateFormat
        override val typeHints = ShortTypeHints(List(classOf[RootEvent], classOf[RootCommand]))
        override val typeHintFieldName = "t"
    } + SingleStringCaseClassSerializer[TestRootId]()
}
