package com.pointswarm.common.dtos

case class EventId(value: String)
{
    assert(value != null && value.trim.nonEmpty)
    override def toString = value
}

object EventId
{
    implicit def fromString(s: String): EventId = EventId(s)
    implicit def toString(id: EventId): String = id.value
}