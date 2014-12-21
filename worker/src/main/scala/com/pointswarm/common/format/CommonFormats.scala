package com.pointswarm.common.format

import com.dmitryegorov.tools.serialization.SingleStringCaseClassSerializer._
import com.dmitryegorov.tools.serialization._
import com.pointswarm.commands.ProviderType
import com.pointswarm.common.dtos._
import com.pointswarm.domain.reporting.Report
import com.pointswarm.domain.reporting.Report.{DoReport, Created, Edited}
import com.pointswarm.domain.voting.Upvote.{Cancelled, Casted}
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
                               classOf[Report.Id],
                               classOf[DoReport],
                               classOf[Created],
                               classOf[Edited],
                               classOf[Casted],
                               classOf[Cancelled]
                           )) +
                           SingleStringCaseClassSerializer[EventId](x => EventId(x)) +
                           SingleStringCaseClassSerializer[ReportId](x => ReportId(x)) +
                           SingleStringCaseClassSerializer[ProfileId](x => ProfileId(x)) +
                           SingleStringCaseClassSerializer[CommandName](x => CommandName(x)) +
                           SingleStringCaseClassSerializer[MinionName](x => MinionName(x)) +
                           SingleStringCaseClassSerializer[HistoryId](x => HistoryId(x)) +
                           SingleStringCaseClassSerializer[HtmlContent](x => HtmlContent(x)) +
                           SingleStringCaseClassSerializer[AccountId](x => AccountId(x)) +
                           SingleStringCaseClassSerializer[Name](x => Name(x)) +
                           new DateTimeSerializer +
                           new EnumNameSerializer(ProviderType)
}






