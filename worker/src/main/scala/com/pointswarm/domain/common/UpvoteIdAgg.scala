package com.pointswarm.domain.common

import com.scalasourcing.model.id.CompositeAggregateId

case class UpvoteIdAgg(voterId: ProfileIdAgg, reportId: ReportIdAgg) extends CompositeAggregateId
{
    val ids = Seq(voterId, reportId)
}
