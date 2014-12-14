package com.pointswarm.common.format

import com.pointswarm.common.dtos._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.serialization.SingleStringCaseClassSerializer._
import com.pointswarm.tools.serialization._
import org.json4s._
import org.json4s.jackson.Serialization

object CommonFormats
{
    implicit val formats = Serialization.formats(NoTypeHints) +
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
                           new ProviderTypeSerializer

}




