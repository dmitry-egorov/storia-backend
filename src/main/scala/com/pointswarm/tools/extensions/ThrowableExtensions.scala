package com.pointswarm.tools.extensions

import java.io.{PrintWriter, StringWriter}

object ThrowableExtensions
{
    implicit class ThrowableEx(val t: Throwable) extends AnyVal
    {
        def fullMessage: String =
        {
            val sw = new StringWriter()
            val pw = new PrintWriter(sw)
            t.printStackTrace(pw)
            sw.toString
        }
    }
}
