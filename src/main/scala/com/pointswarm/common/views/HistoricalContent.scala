package com.pointswarm.common.views

import com.pointswarm.common.dtos._
import org.joda.time._

case class HistoricalContent(content: HtmlContent, addedOn: DateTime)

object HistoricalContent
{
    def apply(content: HtmlContent) = new HistoricalContent(content, DateTime.now(DateTimeZone.UTC))
}