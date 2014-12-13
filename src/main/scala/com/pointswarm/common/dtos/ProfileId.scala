package com.pointswarm.common.dtos

case class ProfileId(value: String) extends AnyVal
{
    override def toString = value
}

object ProfileId
{
    implicit def fromString(s: String): ProfileId = new ProfileId(s)
    implicit def toString(id: ProfileId): String = id.value
}