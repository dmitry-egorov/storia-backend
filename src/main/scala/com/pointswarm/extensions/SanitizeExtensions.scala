package com.pointswarm.extensions

object SanitizeExtensions
{
    implicit class StringSanitizer(str: String)
    {
        def sanitize =
            str
                .replaceAll("\\s+", "-")
                .replaceAll("[^A-Za-z0-9-]", "")
                .toLowerCase
    }

}
