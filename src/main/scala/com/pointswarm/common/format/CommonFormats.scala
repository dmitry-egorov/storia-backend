package com.pointswarm.common.format

import com.pointswarm.commands.ProviderType._
import com.pointswarm.common.dtos._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.serialization.SingleStringCaseClassSerializer._
import com.pointswarm.tools.serialization._
import org.json4s.JsonAST._
import org.json4s._
import org.json4s.jackson.Serialization

object CommonFormats
{
    implicit val formats = Serialization.formats(NoTypeHints) +
                           SingleStringCaseClassSerializer[EventId](x => new EventId(x)) +
                           SingleStringCaseClassSerializer[ReportId](x => new ReportId(x)) +
                           SingleStringCaseClassSerializer[ProfileId](x => new ProfileId(x)) +
                           SingleStringCaseClassSerializer[CommandName](x => new CommandName(x)) +
                           SingleStringCaseClassSerializer[MinionName](x => new MinionName(x)) +
                           SingleStringCaseClassSerializer[HistoryId](x => new HistoryId(x)) +
                           SingleStringCaseClassSerializer[HtmlContent](x => new HtmlContent(x)) +
                           new DateTimeSerializer +
                           new ProviderTypeSerializer

}




