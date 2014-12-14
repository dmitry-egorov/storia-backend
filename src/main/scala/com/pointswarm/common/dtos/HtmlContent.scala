package com.pointswarm.common.dtos

case class HtmlContent(value: String)
{
    assert(value != null && value.trim.nonEmpty)
    override def toString = value
}

object HtmlContent
{
    implicit def fromString(s: String): HtmlContent = HtmlContent(s)
    implicit def toString(id: HtmlContent): String = id.value
}