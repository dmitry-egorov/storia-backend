package com.scalasourcing.backend

import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.dmitryegorov.futuristic.FutureExtensions._
import rx.lang.scala.Observable

import scala.concurrent.ExecutionContext
import scala.util.Try

case class CompositeExecutor[T](executors: Seq[Executor[T]] = Seq.empty)(implicit ec: ExecutionContext) extends Executor[T]
{
    def and(executor: Executor[T]): CompositeExecutor[T] =
    {
        copy(executors = executors ++ Seq(executor))
    }

    def run(completeWith: CancellationToken): Observable[Try[T]] =
    {
        val all = executors.map(e => e.run(completeWith))
        Observable.from(all).flatten
    }

    def prepare(completeWith: CancellationToken) =
    {
        executors.map(x => x.prepare(completeWith)).waitAll
    }
}
