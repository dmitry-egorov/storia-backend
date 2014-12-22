package com.pointswarm.common.dtos

case class ReportId(value: String) {
    assert(value != null && value.trim.nonEmpty)
    override def toString = value
}

object ReportId {
    implicit def fromString(s: String): ReportId = ReportId(s)
    implicit def toString(id: ReportId): String = id.value
}

