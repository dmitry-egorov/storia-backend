package com.pointswarm.common.dtos

import com.dmitryegorov.tools.extensions.SanitizeExtensions._

case class Name(content: String)
{
    assert(content != null && content.trim.nonEmpty)
    override def toString = content
    def sanitize = content.sanitize
}

object Name
{
    implicit def fromString(s: String): Name = Name(s)
    implicit def toString(id: Name): String = id.content
}
