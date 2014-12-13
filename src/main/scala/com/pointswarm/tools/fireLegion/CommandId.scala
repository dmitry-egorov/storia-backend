package com.pointswarm.tools.fireLegion

case class CommandId(value: String) extends AnyVal
{
    override def toString = value
}

object CommandId
{
    implicit def fromString(s: String): CommandId = new CommandId(s)
    implicit def toString(id: CommandId): String = id.value
}
