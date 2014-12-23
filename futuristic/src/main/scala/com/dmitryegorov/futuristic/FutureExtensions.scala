package com.dmitryegorov.futuristic

import rx.lang.scala.Observable

import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

object FutureExtensions
{
    implicit class FutureTryEx[T](val future: Future[Try[T]]) extends AnyVal
    {
        def flatRecoverAsTry(implicit ec: ExecutionContext): Future[Try[T]] = future.recoverAsTry.map(_.flatten)
    }

    implicit class FutureEx[T](val future: Future[T]) extends AnyVal
    {
        def recoverAsTry(implicit ec: ExecutionContext): Future[Try[T]] =
            future
            .map
            {
                case response => Success(response)
            }
            .recover
            {
                case cause => Failure(cause)
            }


        def timeout(duration: Duration)(implicit ec: ExecutionContext): Future[T] =
        {
            Future.firstCompletedOf(List(future, Futuristic.timeoutFail[T](duration)))
        }

        def observe(implicit ec: ExecutionContext): Observable[T] = Observable.from(future)
    }

    implicit class SeqFutureEx[T](val futures: Seq[Future[T]]) extends AnyVal
    {
        def waitAll(implicit ec: ExecutionContext) = Future.sequence(futures).map(_ => ())
        def whenAll(implicit ec: ExecutionContext) = Future.sequence(futures)
    }

    implicit class TryEx[T](val t: Try[T]) extends AnyVal
    {
        def asFuture: Future[T] = t match
        {
            case Success(value) => Future.successful(value)
            case Failure(cause) => Future.failed(cause)
        }
    }

}


