package com.pointswarm.domain.common

import com.scalasourcing.model.id.CompositeAggregateId

case class ReportIdAgg(authorId: ProfileIdAgg, eventId: EventIdAgg) extends CompositeAggregateId
{
    val ids = Seq(authorId, eventId)
}
