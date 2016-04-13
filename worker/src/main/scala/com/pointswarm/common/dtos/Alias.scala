package com.pointswarm.common.dtos

case class Alias(value: String)
{
    assert(value != null && value.trim.nonEmpty)
    override def toString = value
}

object Alias
{
    implicit def fromString(s: String): Alias = Alias(s)
    implicit def toString(alias: Alias): String = alias.value
}
