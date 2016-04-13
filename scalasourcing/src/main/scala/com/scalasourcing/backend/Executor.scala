package com.scalasourcing.backend

import com.dmitryegorov.futuristic.cancellation.CancellationToken
import rx.lang.scala.Observable

import scala.concurrent.Future
import scala.util.Try

trait Executor
{
    def run(completeWith: CancellationToken): Observable[Try[ExecutionResult]]
    def prepare(completeWith: CancellationToken) : Future[Unit]
}
