package com.pointswarm.common.dtos

case class EventId(value: String) extends AnyVal
{
    override def toString = value
}

object EventId
{
    implicit def fromString(s: String): EventId = new EventId(s)
    implicit def toString(id: EventId): String = id.value
}