package com.scalasourcing.backend.firebase

import com.firebase.client.Firebase
import com.scalasourcing.backend.{CompositeExecutor, Executor}
import com.scalasourcing.model.Aggregate
import org.json4s.Formats

import scala.concurrent.ExecutionContext

object FirebaseExecutorsBuilder {
    def apply(fb: Firebase)(implicit f: Formats, ec: ExecutionContext)
    = new FirebaseExecutorsBuilder(Seq.empty, fb)
}

case class FirebaseExecutorsBuilder
(executors: Seq[Executor], fb: Firebase)
(implicit f: Formats, ec: ExecutionContext) {
    def and(agg: Aggregate)(implicit m21: Manifest[agg.Id], m22: Manifest[agg.Command]) = {
        val fes = FirebaseEventStorage(agg)(fb)
        val executor = FirebaseExecutor(agg)(fb, fes)

        copy(executors = executors ++ Seq(executor))
    }

    def build: CompositeExecutor = new CompositeExecutor(executors)
}
