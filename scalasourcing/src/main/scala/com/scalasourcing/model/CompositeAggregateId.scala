package com.scalasourcing.model

import com.scalasourcing.model.Aggregate.AggregateId

trait CompositeAggregateId extends AggregateId
{
    def ids: Seq[AggregateId]
    def value = ids.map(_.value).mkString("+")
}
