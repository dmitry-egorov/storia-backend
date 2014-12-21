package com.scalasourcing.backend.firebase

import java.beans.Introspector

import scala.concurrent.{ExecutionContext, Future}

object Tools
{
    implicit class RichSeqFuture[T](val futures: Seq[Future[T]]) extends AnyVal
    {
        def waitAll(implicit ec: ExecutionContext) = Future.sequence(futures).map(_ => ())
        def whenAll(implicit ec: ExecutionContext) = Future.sequence(futures)
    }

    implicit class RichString(val s: String) extends AnyVal
    {
        def decapitalize =
        {
            Introspector.decapitalize(s)
        }
    }
}
