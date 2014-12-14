package com.pointswarm.commands

import com.pointswarm.common.dtos._

case class SortReportsCommand(eventId: Option[EventId], reportId: Option[ReportId])
{
    assert(eventId != null)
    assert(reportId != null)
    assert(eventId.isDefined || reportId.isDefined)
}
