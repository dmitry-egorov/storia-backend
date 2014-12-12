package com.pointswarm.tools.processing

case class CommandId(value: String)
{
    override def toString = value
}

object CommandId
{
    implicit def fromString(s: String): CommandId = new CommandId(s)
    implicit def toString(id: CommandId): String = id.value
}
