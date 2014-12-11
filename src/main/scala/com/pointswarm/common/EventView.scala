package com.pointswarm.common

import org.joda.time.DateTime

case class EventView(title: String, addedOn: DateTime, previewId: Option[ReportId], reports: List[ReportId])
