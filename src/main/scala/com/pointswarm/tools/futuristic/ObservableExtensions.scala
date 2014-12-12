package com.pointswarm.tools.futuristic

import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import rx.lang.scala.Observable

import scala.concurrent._

object ObservableExtensions
{

    implicit class ObservableEx[T](obs: Observable[T])(implicit ec: ExecutionContext)
    {
        def completeWith(token: CancellationToken) =
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

        def cancelWith(token: CancellationToken) =
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

        def flatMapF[U](f: T => Future[U]) = obs.flatMap(x => Observable.from(f(x)))

        def concatMapF[U](f: T => Future[U]) = obs.concatMap(x => Observable.from(f(x)))

        def countF(p: T => Boolean): Future[Int] =
        {
            obs.count(p).firstF
        }

        def countF: Future[Int] =
        {
            obs.count(_ => true).firstF
        }

        def lastOrElseF(default: => T): Future[T] =
        {
            obs.lastOrElse(default).firstF
        }

        def lastOptionF: Future[Option[T]] =
        {
            obs.lastOption.firstF
        }

        def firstF: Future[T] =
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
