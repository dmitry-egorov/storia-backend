package com.dmitryegorov.tools.futuristic

import com.dmitryegorov.tools.futuristic.cancellation.CancellationToken
import rx.lang.scala.Observable

import scala.concurrent._

object ObservableExtensions
{

    implicit class ObservableEx[T](val obs: Observable[T]) extends AnyVal
    {
        def completeWith(token: CancellationToken)(implicit ec: ExecutionContext) =
        {
            Observable.create[T](
                observer =>
                {
                    val subs = obs.subscribe(observer)

                    token.whenCancelled(
                        () =>
                        {
                            subs.unsubscribe()
                            observer.onCompleted()
                        })

                    subs
                })
        }

        def cancelWith(token: CancellationToken)(implicit ec: ExecutionContext) =
        {
            Observable.create[T](
                observer =>
                {
                    val subs = obs.subscribe(observer)

                    token.whenCancelled(
                        () =>
                        {
                            subs.unsubscribe()
                            observer.onError(new CancellationException)
                        })

                    subs
                })
        }

        def flatMapF[U](f: T => Future[U])(implicit ec: ExecutionContext) = obs.flatMap(x => Observable.from(f(x)))

        def concatMapF[U](f: T => Future[U])(implicit ec: ExecutionContext) = obs.concatMap(x => Observable.from(f(x)))

        def countF(p: T => Boolean)(implicit ec: ExecutionContext): Future[Int] =
        {
            obs.count(p).firstF
        }

        def countF(implicit ec: ExecutionContext): Future[Int] =
        {
            obs.count(_ => true).firstF
        }

        def lastOrElseF(default: => T)(implicit ec: ExecutionContext): Future[T] =
        {
            obs.lastOrElse(default).firstF
        }

        def lastOptionF(implicit ec: ExecutionContext): Future[Option[T]] =
        {
            obs.lastOption.firstF
        }

        def firstF(implicit ec: ExecutionContext): Future[T] =
        {
            val p = Promise[T]()

            val subs = obs.first.subscribe(
                o => p.success(o),
                ex => p.failure(ex)
            )

            val f = p.future

            f.onComplete(_ => subs.unsubscribe())

            f
        }
    }

}
