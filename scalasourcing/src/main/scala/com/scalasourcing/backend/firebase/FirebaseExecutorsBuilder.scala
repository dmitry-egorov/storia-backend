package com.scalasourcing.backend.firebase

import com.firebase.client.Firebase
import com.scalasourcing.backend.{CompositeExecutor, EventStorage, Executor}
import com.scalasourcing.model.Aggregate.{Factory, IdOf}
import com.scalasourcing.model.AggregateRoot
import org.json4s.Formats

import scala.concurrent.ExecutionContext

case class FirebaseExecutorsBuilder
(executors: Seq[Executor], fb: Firebase, es: EventStorage)
(implicit f: Formats, ec: ExecutionContext)
{
    def and[Id <: IdOf[Root] : Manifest, Root <: AggregateRoot[Root] : Manifest : Factory] =
    {
        copy(executors = executors ++ Seq(new FirebaseExecutor[Id, Root](fb, es)))
    }

    def build: CompositeExecutor = new CompositeExecutor(executors)
}

object FirebaseExecutorsBuilder
{
    def apply
    (fb: Firebase, es: EventStorage)
    (implicit f: Formats, ec: ExecutionContext) = new FirebaseExecutorsBuilder(Seq.empty, fb, es)
}
