package com.pointswarm.common.dtos


case class HtmlContent(value: String) extends NonEmptyStringContent
{
    private val limit = 65000

    require(value.length <= limit)
    override def toString = value
}

object HtmlContent
{

    implicit def fromString(value: String): HtmlContent = HtmlContent(value)

    implicit def toString(content: HtmlContent): String = content.value
}