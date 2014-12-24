package com.scalasourcing.tools

import java.beans.Introspector

object StringExtensions
{
    implicit class RichString(val s: String) extends AnyVal
    {
        def decapitalize =
        {
            Introspector.decapitalize(s)
        }

        def hash =
        {
            val m = java.security.MessageDigest.getInstance("MD5")
            val b = s.getBytes("UTF-8")
            m.update(b, 0, b.length)
            new java.math.BigInteger(1, m.digest()).toString(16)
        }
    }
}
