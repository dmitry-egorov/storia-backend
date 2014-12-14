package com.pointswarm.common.dtos

case class ProfileId(value: String)
{
    assert(value != null && value.trim.nonEmpty)
    override def toString = value
}

object ProfileId
{
    implicit def fromString(s: String): ProfileId = ProfileId(s)
    implicit def toString(id: ProfileId): String = id.value
}