package com.pointswarm.common.dtos

import com.dmitryegorov.tools.extensions.SanitizeExtensions._

case class Name(value: String)
{
    assert(value != null && value.trim.nonEmpty)
    override def toString = value
    def alias = Alias(value.sanitize)
}

object Name
{
    implicit def fromString(s: String): Name = Name(s)
    implicit def toString(id: Name): String = id.value
}


