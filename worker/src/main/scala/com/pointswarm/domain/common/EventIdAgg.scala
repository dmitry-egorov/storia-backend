package com.pointswarm.domain.common

import com.scalasourcing.model.id.StringAggregateId

case class EventIdAgg(value: String) extends StringAggregateId
{
    assert(value != null && value.trim.nonEmpty)
}

object EventIdAgg
{
    implicit def fromString(s: String): EventIdAgg = EventIdAgg(s)
    implicit def toString(id: EventIdAgg): String = id.value
}