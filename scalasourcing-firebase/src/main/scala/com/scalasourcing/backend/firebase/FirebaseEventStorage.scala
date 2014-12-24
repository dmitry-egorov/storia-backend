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
    private lazy val aggregatesVersionsRef = fb / "aggregateVersions" / a.name
    private lazy val aggregatesIdsRef = fb / "aggregateIds" / a.name
    private lazy val aggregatesEventsRef = fb / "aggregateEvents" / a.name
    private def aggregateEventsRef(id: a.Id): Firebase = aggregatesEventsRef / id.hash
    private def aggregateVersionRef(id: a.Id): Firebase = aggregatesVersionsRef / id.hash
    private def aggregateIdRef(id: a.Id): Firebase = aggregatesIdsRef / id.hash

    def get(id: a.Id): Future[a.EventsSeq] =
    {
        aggregateEventsRef(id)
        .value[Seq[AnyRef]]
        .map(x => x.getOrElse(Seq.empty))
        .mapTo[a.EventsSeq]
    }

    def tryPersist(id: a.Id, events: a.EventsSeq, expectedVersion: Int): Future[Boolean] =
    {
        for
        {
            committed <- tryCommitVersion(id, expectedVersion, events.length)
            r <- trySaveEvents(id, events, expectedVersion, committed)
        }
        yield r
    }

    private def tryCommitVersion(id: a.Id, expectedVersion: Int, eventCount: Int): Future[Boolean] =
    {
        aggregateVersionRef(id)
        .transaction[Int](v => tryGetNewVersion(expectedVersion, v.getOrElse(0), eventCount))
    }

    private def trySaveEvents(id: a.Id, events: a.EventsSeq, expectedVersion: Int, committed: Boolean): Future[Boolean] =
    {
        if (committed)
        {
            val fsa = saveId(id)
            val fse = saveEvents(id, events, expectedVersion)

            Seq(fsa, fse).waitAll.map(_ => true)
        }
        else Future.successful(false)
    }

    private def tryGetNewVersion(expectedVersion: Int, currentVersion: Int, count: Int): Option[Int] =
    {
        if (currentVersion == expectedVersion)
            Some(currentVersion + count)
        else
            None
    }

    private def saveId(id: a.Id): Future[Unit] =
    {
        (aggregateIdRef(id) <-- id).map(_ => ())
    }

    private def saveEvents(id: a.Id, events: a.EventsSeq, expectedVersion: Int): Future[Unit] =
    {
        events
        .zipWithIndex
        .map(e => saveEvent(id, expectedVersion + e._2, e._1))
        .waitAll
    }

    private def saveEvent(id: a.Id, eventIndex: Int, event: a.Event): Future[String] =
    {
        val index = eventIndex.toString
        aggregateEventsRef(id) / index <-- event
    }
}
