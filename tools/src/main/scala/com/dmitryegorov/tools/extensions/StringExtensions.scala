package com.dmitryegorov.tools.extensions

import java.beans.Introspector

object StringExtensions
{
    implicit class StringEx(val s: String) extends AnyVal
    {
        def decapitalize =
        {
            Introspector.decapitalize(s)
        }
    }
}
