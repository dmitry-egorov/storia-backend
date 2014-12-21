package com.scalasourcing.backend.firebase

import com.firebase.client.Firebase
import com.scalasourcing.backend.{CompositeExecutor, EventStorage, Executor}
import com.scalasourcing.model.{Aggregate, AggregateRoot}
import org.json4s.Formats

import scala.concurrent.ExecutionContext

case class FirebaseExecutorsBuilder
(executors: Seq[Executor], fb: Firebase, es: EventStorage)
(implicit f: Formats, ec: ExecutionContext)
{
    def and[Root <: AggregateRoot[Root] : Manifest: Aggregate](a: Aggregate[Root])(implicit m:Manifest[a.Id]) =
    {
        copy(executors = executors ++ Seq(FirebaseExecutor(a)(fb, es)))
    }

    def build: CompositeExecutor = new CompositeExecutor(executors)
}

object FirebaseExecutorsBuilder
{
    def apply
    (fb: Firebase, es: EventStorage)
    (implicit f: Formats, ec: ExecutionContext) = new FirebaseExecutorsBuilder(Seq.empty, fb, es)
}
