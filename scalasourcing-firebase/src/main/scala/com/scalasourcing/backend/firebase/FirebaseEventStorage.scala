package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.EventStorage
import com.scalasourcing.backend.firebase.StringExtensions._
import com.scalasourcing.model.Aggregate.AggregateId
import com.scalasourcing.model.{Aggregate, AggregateRoot}
import org.json4s.Formats

import scala.concurrent._

class FirebaseEventStorage(fb: Firebase)(implicit val ec: ExecutionContext, f: Formats) extends EventStorage
{
    private lazy val aggregatesRef = fb / "aggregates"

    def get[R <: AggregateRoot[R]](a: Aggregate[R]): a.Id => Future[a.EventsSeq] =
    {
        id =>
        (aggregateRef(a, id) / "events")
        .value[Seq[AnyRef]]
        .map(x => x.getOrElse(Seq.empty))
        .mapTo[a.EventsSeq]
    }

    def tryPersist[R <: AggregateRoot[R]](a: Aggregate[R]): (a.Id, a.EventsSeq, Int) => Future[Boolean] =
    {
        (id, events, expectedVersion) =>

        val ar = aggregateRef(a, id)

        val versionRef = ar / "version"

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
                        .map(e => ar / "events" / (expectedVersion + e._2) <-- e._1)
                        .waitAll
                        .map(_ => true)
                    }
                    else
                    {
                        Future.successful(false)
                    }
                })
    }

    private def aggregateRef[AR <: AggregateRoot[AR]](a: Aggregate[AR], id: AggregateId): Firebase =
    {
        val name = a.getClass.getSimpleName.replace("$", "").decapitalize
        val value = id.value

        aggregatesRef / name / value
    }
}
