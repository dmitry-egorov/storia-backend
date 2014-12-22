package com.scalasourcing.tools

import java.beans.Introspector

object StringExtensions {
    implicit class RichString(val s: String) extends AnyVal {
        def decapitalize = {
            Introspector.decapitalize(s)
        }
    }
}
