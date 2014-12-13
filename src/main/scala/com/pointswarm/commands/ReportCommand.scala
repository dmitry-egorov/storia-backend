package com.pointswarm.commands

import com.pointswarm.common.dtos._
import org.joda.time._

case class ReportCommand(authorId: ProfileId, eventId: EventId, content: HtmlContent, addedOn: DateTime)