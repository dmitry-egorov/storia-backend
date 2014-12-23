package com.pointswarm.common.format

import com.dmitryegorov.tools.serialization.SingleStringCaseClassSerializer._
import com.dmitryegorov.tools.serialization._
import com.pointswarm.commands.ProviderType
import com.pointswarm.common.dtos._
import com.pointswarm.domain.reporting.Report.{Added, DoReport, Edited}
import com.pointswarm.fireLegion._
import org.json4s._
import org.json4s.ext.EnumNameSerializer

object CommonFormats
{
    implicit val formats = new Formats
    {
        val dateFormat = DefaultFormats.lossless.dateFormat
        override val typeHintFieldName = "t"
    } +
                           ShortTypeHints(List(
                               classOf[DoReport],
                               classOf[Added],
                               classOf[Edited]
                           )) +
                           SingleStringCaseClassSerializer[EventId]() +
                           SingleStringCaseClassSerializer[ReportId]() +
                           SingleStringCaseClassSerializer[ProfileId]() +
                           SingleStringCaseClassSerializer[CommandName]() +
                           SingleStringCaseClassSerializer[MinionName]() +
                           SingleStringCaseClassSerializer[HistoryId]() +
                           SingleStringCaseClassSerializer[HtmlContent]() +
                           SingleStringCaseClassSerializer[AccountId]() +
                           SingleStringCaseClassSerializer[Name]() +
                           new DateTimeSerializer +
                           new EnumNameSerializer(ProviderType)
}






