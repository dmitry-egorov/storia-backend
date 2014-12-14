package com.pointswarm.common.views

import com.pointswarm.common.dtos._
import org.joda.time._

case class EventView(title: Name, addedOn: DateTime, previewId: Option[ReportId], reports: Option[Map[ReportId, Boolean]])
{
    assert(title != null)
}

object EventView
{
    def apply(title: Name): EventView =
    {
        EventView(title, DateTime.now(DateTimeZone.UTC), None, None)
    }
}
