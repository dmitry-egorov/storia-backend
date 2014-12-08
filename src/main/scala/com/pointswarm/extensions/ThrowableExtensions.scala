package com.pointswarm.extensions

import java.io.{PrintWriter, StringWriter}

object ThrowableExtensions
{
    implicit class ThrowableEx(t: Throwable)
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
