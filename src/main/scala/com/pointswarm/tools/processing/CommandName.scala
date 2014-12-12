package com.pointswarm.tools.processing

case class CommandName(value: String)
{
    override def toString = value
}

object CommandName
{
    implicit def fromString(s: String): CommandName = new CommandName(s)
    implicit def toString(cn: CommandName): String = cn.value
}
