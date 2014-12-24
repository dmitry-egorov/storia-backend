package com.pointswarm.domain.common

import com.scalasourcing.model.id.StringAggregateId

case class ProfileIdAgg(value: String) extends StringAggregateId
{
    assert(value != null && value.trim.nonEmpty)
}

object ProfileIdAgg
{
    implicit def fromString(s: String): ProfileIdAgg = ProfileIdAgg(s)
    implicit def toString(id: ProfileIdAgg): String = id.value
}