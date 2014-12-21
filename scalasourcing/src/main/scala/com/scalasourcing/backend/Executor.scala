package com.scalasourcing.backend

import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.scalasourcing.model.Aggregate.AggregateCommandResult
import rx.lang.scala.Observable

import scala.util.Try

trait Executor
{
    def run(completeWith: CancellationToken): Observable[Try[AggregateCommandResult]]
}
