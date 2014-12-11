package com.pointswarm.tools.extensions

import java.beans.Introspector

object StringExtensions
{
    implicit class StringEx(s: String)
    {
        def decapitalize =
        {
            Introspector.decapitalize(s)
        }
    }
}
