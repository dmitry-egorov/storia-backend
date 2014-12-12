package com.pointswarm.tools.processing

case class MinionName(value: String)
{
    override def toString: String = value
}
object MinionName
{
    implicit def fromString(s: String): MinionName = new MinionName(s)
    implicit def toString(mn: MinionName): String = mn.value
}
