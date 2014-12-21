package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client._
import com.scalasourcing.backend.EventStorage
import com.scalasourcing.backend.firebase.StringExtensions._
import com.scalasourcing.model.Aggregate._
import org.json4s.Formats

import scala.concurrent._

class FirebaseEventStorage(fb: Firebase)(implicit val ec: ExecutionContext, f: Formats) extends EventStorage
{
    private lazy val aggregatesRef = fb / "aggregates"

    override def get[AR: Manifest](id: IdOf[AR]): Future[EventsSeqOf[AR]] =
    {
        (aggregateRef(id) / "events")
        .value[Seq[AnyRef]]
        .map(x => x.getOrElse(Seq.empty))
        .mapTo[EventsSeqOf[AR]]
    }

    override def tryPersist[AR: Manifest](id: IdOf[AR], events: EventsSeqOf[AR], expectedVersion: Int): Future[Boolean] =
    {
        val ar = aggregateRef(id)

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

    private def aggregateRef[AR: Manifest](id: IdOf[AR]): Firebase =
    {
        val name = implicitly[Manifest[AR]].runtimeClass.getSimpleName.decapitalize
        val value = id.value

        aggregatesRef / name / value
    }
}
