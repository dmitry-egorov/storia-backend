package com.pointswarm.tools.extensions

import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

object ObservableExtensions
{
    implicit class ObservableEx[T](observable: Observable[T])
    {
        def futureMap[U](f: T => Future[U]) = observable.flatMap(x => Observable.from(f(x)))
    }
}
