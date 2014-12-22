package com.dmitryegorov.tools.extensions

import hr.element.etb.translit.SlugURL

object SanitizeExtensions {
    private lazy val slug = new SlugURL("Cyrillic-Latin; Latin-ASCII", "-")
    implicit class StringSanitizer(val str: String) extends AnyVal {
        def sanitize: String = slug(str)
    }
}
