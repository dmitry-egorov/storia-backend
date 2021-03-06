package com.scalasourcing.backend.memory

import com.scalasourcing.backend.EventStorage
import com.scalasourcing.model.Aggregate

import scala.collection.concurrent._
import scala.concurrent.{ExecutionContext, Future}

class SingleThreadInMemoryEventStorage[A <: Aggregate](ag: A)(implicit ec: ExecutionContext) extends EventStorage[A](ag)
{
    private val aggregatesEventsMap: Map[a.Id, a.EventsSeq] = TrieMap.empty

    def get(id: a.Id): Future[a.EventsSeq] =
    {
        Future.successful(getEventsSeq(id))
    }

    def tryPersist(id: a.Id, events: a.EventsSeq, expectedVersion: Int): Future[Boolean] =
    {
        val eventsSeq = getEventsSeq(id)

        aggregatesEventsMap(id) = eventsSeq ++ events //Not thread safe

        Future.successful(true)
    }

    private def getEventsSeq(id: a.Id): a.EventsSeq =
    {
        aggregatesEventsMap.getOrElse(id, Seq.empty)
    }
}
