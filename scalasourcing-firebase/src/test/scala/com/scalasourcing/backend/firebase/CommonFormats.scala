package com.scalasourcing.backend.firebase

import com.scalasourcing.backend.Tester.{DoSomething, SomethingHappened}
import com.scalasourcing.backend.TesterId
import com.scalasourcing.backend.firebase.SingleStringCaseClassSerializer._
import org.json4s.{DefaultFormats, Formats, ShortTypeHints}

object CommonFormats
{
    def formats: Formats = new Formats
    {
        val dateFormat = DefaultFormats.lossless.dateFormat
        override val typeHints = ShortTypeHints(List(SomethingHappened.getClass, DoSomething.getClass))
        override val typeHintFieldName = "t"
    } + SingleStringCaseClassSerializer[TesterId]()
}
