package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.EventStorage
import com.scalasourcing.model.Aggregate
import org.json4s.Formats

import scala.concurrent._

object FirebaseEventStorage
{
    def apply(agg: Aggregate)(fb: Firebase)(implicit ec: ExecutionContext, f: Formats): FirebaseEventStorage {val a: agg.type } =
    {
        new FirebaseEventStorage(fb) {
            override val a: agg.type = agg
        }
    }
}

abstract class FirebaseEventStorage(fb: Firebase)(implicit val ec: ExecutionContext, f: Formats) extends EventStorage
{
    private lazy val aggregatesRef = fb / "aggregates" / a.name

    def get(id: a.Id): Future[a.EventsSeq] =
    {
        (aggregateRef(id) / "events")
        .value[Seq[AnyRef]]
        .map(x => x.getOrElse(Seq.empty))
        .mapTo[a.EventsSeq]
    }

    def tryPersist(id: a.Id, events: a.EventsSeq, expectedVersion: Int): Future[Boolean] =
    {
        val ar = aggregateRef(id)

        val versionRef = ar / "version"
        val eventsRef = ar / "events"

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
                        .map(e => {
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

    private def aggregateRef(id: a.Id): Firebase =
    {
        aggregatesRef / id.value
    }
}
