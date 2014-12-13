package com.pointswarm.tools.fireLegion

import com.pointswarm.tools.extensions.StringExtensions._

case class CommandName(value: String) extends AnyVal
{
    override def toString = value
}

object CommandName
{
    def of[TCommand](implicit m: Manifest[TCommand]): CommandName =
    {
        m
        .runtimeClass
        .getSimpleName
        .replaceAll("Command", "")
        .decapitalize
    }

    implicit def fromString(s: String): CommandName = new CommandName(s)
    implicit def toString(cn: CommandName): String = cn.value
}
