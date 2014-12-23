package com.scalasourcing.backend.firebase

case class CommandId(value: String)
{
    assert(value != null && value.nonEmpty)
    override def toString = value
}

object CommandId
{
    implicit def fromString(s: String): CommandId = CommandId(s)
    implicit def toString(id: CommandId): String = id.value
}