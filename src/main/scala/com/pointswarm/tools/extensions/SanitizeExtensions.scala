package com.pointswarm.tools.extensions

import hr.element.etb.translit.SlugURL

object SanitizeExtensions
{
    implicit class StringSanitizer(val str: String) extends AnyVal
    {
        def sanitize = SlugURL(str)
    }
}
