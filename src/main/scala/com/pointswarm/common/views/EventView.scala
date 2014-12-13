package com.pointswarm.common.views

import com.pointswarm.common.dtos._
import org.joda.time._

case class EventView(title: String, addedOn: DateTime, previewId: Option[ReportId], reports: Set[ReportId])

object EventView
{
    def from(title: String): EventView =
    {
        new EventView(title, DateTime.now(DateTimeZone.UTC), None, Set.empty)
    }
}
