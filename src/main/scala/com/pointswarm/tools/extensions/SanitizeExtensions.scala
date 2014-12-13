package com.pointswarm.tools.extensions

object SanitizeExtensions
{
    implicit class StringSanitizer(val str: String) extends AnyVal
    {
        def sanitize =
            str
                .replaceAll("\\s+", "-")
                .replaceAll("[^A-Za-z0-9-]", "")
                .toLowerCase
    }

}
