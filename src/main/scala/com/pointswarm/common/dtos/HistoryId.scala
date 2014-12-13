package com.pointswarm.common.dtos

case class HistoryId(value: String)
{
    assert(value != null && value.trim.nonEmpty)
    override def toString = value
}

object HistoryId
{
    implicit def fromString(s: String): HistoryId = new HistoryId(s)
    implicit def toString(id: HistoryId): String = id.value
}
