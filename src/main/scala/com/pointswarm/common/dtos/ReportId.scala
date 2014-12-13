package com.pointswarm.common.dtos

case class ReportId(value: String) extends AnyVal
{
    override def toString = value
}

object ReportId
{
    implicit def fromString(s: String): ReportId = new ReportId(s)
    implicit def toString(id: ReportId): String = id.value
}

