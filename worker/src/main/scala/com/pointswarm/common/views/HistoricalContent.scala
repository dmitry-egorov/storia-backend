package com.pointswarm.common.views

import com.pointswarm.common.dtos._
import org.joda.time._

case class HistoricalContent(content: HtmlContent, addedOn: DateTime)
{
    assert(content != null)
    assert(addedOn != null)
}

object HistoricalContent
{
    def apply(content: HtmlContent):HistoricalContent = HistoricalContent(content, DateTime.now(DateTimeZone.UTC))
}