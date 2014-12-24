package com.pointswarm.domain.common

import com.scalasourcing.model.id.CompositeAggregateId

case class UpvoteIdAgg(userId: ProfileIdAgg, reportId: ReportIdAgg) extends CompositeAggregateId
{
    val ids = Seq(userId, reportId)
}
