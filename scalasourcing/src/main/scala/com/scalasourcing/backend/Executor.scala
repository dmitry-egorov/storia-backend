package com.scalasourcing.backend

import com.dmitryegorov.futuristic.cancellation.CancellationToken
import rx.lang.scala.Observable

import scala.concurrent.Future
import scala.util.Try

trait Executor[+T]
{
    def run(completeWith: CancellationToken): Observable[Try[T]]
    def prepare(completeWith: CancellationToken) : Future[Unit]
}
