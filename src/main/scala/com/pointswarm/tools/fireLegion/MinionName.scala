package com.pointswarm.tools.fireLegion

case class MinionName(value: String) extends AnyVal
{
    override def toString: String = value
}

object MinionName
{
    implicit def fromString(s: String): MinionName = new MinionName(s)
    implicit def toString(mn: MinionName): String = mn.value
}
