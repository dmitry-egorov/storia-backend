package com.scalasourcing.backend.firebase.domain

import com.scalasourcing.backend.{SubId2, SubId1}
import com.scalasourcing.backend.Tester.{DoSomething, SomethingHappened}
import org.json4s.{DefaultFormats, Formats, ShortTypeHints}
import SingleStringCaseClassSerializer._

object CommonFormats
{
    def formats: Formats = new Formats
    {
        val dateFormat = DefaultFormats.lossless.dateFormat
        override val typeHints = ShortTypeHints(List(SomethingHappened.getClass, DoSomething.getClass))
        override val typeHintFieldName = "t"
    } + SingleStringCaseClassSerializer[SubId1]() + SingleStringCaseClassSerializer[SubId2]() + new DateTimeSerializer()
}
