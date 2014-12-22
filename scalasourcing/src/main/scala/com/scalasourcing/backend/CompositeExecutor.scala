package com.scalasourcing.backend

import com.dmitryegorov.futuristic.cancellation.CancellationToken
import rx.lang.scala.Observable

case class CompositeExecutor(executors: Seq[Executor])
    extends Executor {
    def and(executor: Executor) = {
        copy(executors = executors ++ Seq(executor))
    }

    def run(completeWith: CancellationToken) = Observable.from(executors.map(e => e.run(completeWith))).flatten
}

object CompositeExecutor {
    def apply() = new CompositeExecutor(Seq.empty)
}
