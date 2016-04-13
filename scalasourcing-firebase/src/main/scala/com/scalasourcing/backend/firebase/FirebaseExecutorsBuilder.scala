package com.scalasourcing.backend.firebase

import com.firebase.client.Firebase
import com.scalasourcing.backend._
import com.scalasourcing.model.{Projection, Aggregate}
import org.json4s.Formats

import scala.concurrent.ExecutionContext

case class FirebaseExecutorsBuilder(fb: Firebase, executors: Seq[Executor] = Seq.empty)(implicit f: Formats, ec: ExecutionContext)
{
    def aggregate(agg: Aggregate)(implicit mi: Manifest[agg.Id], mc: Manifest[agg.Command]): FirebaseExecutorsBuilder =
    {
        val eventStorage = new FirebaseEventStorage[agg.type](agg)(fb)
        val commander = FirebaseCommander(agg)(fb, eventStorage)

        executor(commander)
    }

    def projection(agg : Aggregate)(projection: Projection[agg.type])(implicit mi: Manifest[agg.Id], m22: Manifest[agg.Event]): FirebaseExecutorsBuilder =
    {
        val projector = FirebaseProjector(agg)(fb, projection)

        executor(projector)
    }
    
    def executor(e: Executor): FirebaseExecutorsBuilder =
    {
        copy(executors = executors ++ Seq(e))
    }

    def build = new CompositeExecutor(executors)
}
