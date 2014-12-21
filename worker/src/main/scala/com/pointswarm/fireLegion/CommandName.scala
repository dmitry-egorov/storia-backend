package com.pointswarm.fireLegion

import com.dmitryegorov.tools.extensions.StringExtensions._

case class CommandName(value: String)
{
    assert(value != null && value.nonEmpty)
    override def toString = value
}

object CommandName
{
    def apply[TCommand](implicit m: Manifest[TCommand]): CommandName =
    {
        m
        .runtimeClass
        .getSimpleName
        .replaceAll("Command", "")
        .decapitalize
    }

    implicit def fromString(s: String): CommandName = CommandName(s)
    implicit def toString(cn: CommandName): String = cn.value
}
