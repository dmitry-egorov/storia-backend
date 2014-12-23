package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.EventStorage
import com.scalasourcing.model.Aggregate
import org.json4s.Formats

import scala.concurrent._

class FirebaseEventStorage[A <: Aggregate](ag: A)(fb: Firebase)(implicit ec: ExecutionContext, f: Formats) extends EventStorage[A](ag)
{
    private lazy val aggregatesRef = fb / "aggregates" / a.name
    private lazy val aggregatesEventsRef = fb / "aggregateEvents" / a.name

    def get(id: a.Id): Future[a.EventsSeq] =
    {
        aggregateEventsRef(id)
        .value[Seq[AnyRef]]
        .map(x => x.getOrElse(Seq.empty))
        .mapTo[a.EventsSeq]
    }

    def tryPersist(id: a.Id, events: a.EventsSeq, expectedVersion: Int): Future[Boolean] =
    {
        val versionRef = aggregateRef(id) / "version"
        val eventsRef = aggregateEventsRef(id)

        versionRef
        .transaction[Int](
                version =>
                {
                    val v = version.getOrElse(0)
                    val newVersion = v + (events.length: java.lang.Integer)

                    if (v == expectedVersion) Some(newVersion) else None
                })
        .flatMap(
                committed =>
                {
                    if (committed)
                    {
                        events
                        .zipWithIndex
                        .map(e =>
                             {
                                 eventsRef / (expectedVersion + e._2) <-- e._1
                             })
                        .waitAll
                        .map(_ => true)
                    }
                    else
                    {
                        Future.successful(false)
                    }
                })
    }

    private def aggregateEventsRef(id: a.Id): Firebase =
    {
        aggregatesEventsRef / id
    }

    private def aggregateRef(id: a.Id): Firebase =
    {
        aggregatesRef / id
    }
}
