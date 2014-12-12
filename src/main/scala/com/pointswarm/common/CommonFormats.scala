package com.pointswarm.common

import com.pointswarm.tools.processing._
import com.pointswarm.tools.serialization.{DateTimeSerializer, SingleValueCaseClassSerializer}
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization

object CommonFormats
{
    implicit val formats = Serialization.formats(NoTypeHints) +
                           new DateTimeSerializer +
                           new SingleValueCaseClassSerializer[EventId](x => new EventId(x)) +
                           new SingleValueCaseClassSerializer[ReportId](x => new ReportId(x)) +
                           new SingleValueCaseClassSerializer[ProfileId](x => new ProfileId(x)) +
                           new SingleValueCaseClassSerializer[CommandName](x => new CommandName(x)) +
                           new SingleValueCaseClassSerializer[MinionName](x => new MinionName(x))
}
