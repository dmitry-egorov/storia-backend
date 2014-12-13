package com.pointswarm.common.views

import com.pointswarm.common.dtos._
import org.joda.time._

case class ReportView
(
    content: HtmlContent,
    addedOn: DateTime,
    authorId: ProfileId,
    eventId: EventId,
    upvotedBy: Set[ProfileId]
    )
{
    assert(content != null)
    assert(addedOn != null)
    assert(authorId != null)
    assert(eventId != null)
    assert(upvotedBy != null)
}
