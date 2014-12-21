package com.scalasourcing.backend.memory

import com.scalasourcing.backend._
import com.scalasourcing.model.Aggregate._
import com.scalasourcing.model.{Aggregate, AggregateRoot}

import scala.concurrent.{ExecutionContext, Future}

class SingleThreadInMemoryEventStorage(implicit val ec: ExecutionContext) extends EventStorage
{
    private var aggregatesEventsMap: Map[String, Map[AggregateId, Seq[AnyRef]]] = Map.empty

    def get[R <: AggregateRoot[R]](a: Aggregate[R]): a.Id => Future[a.EventsSeq] =
    {
        id =>

        val clazz = getClassName
        val eventsMap = getEventsMap(clazz)
        val events = getEventsSeq(id, eventsMap)

        Future.successful(events.asInstanceOf[a.EventsSeq])
    }

    def tryPersist[R <: AggregateRoot[R]](a: Aggregate[R]): (a.Id, a.EventsSeq, Int) => Future[Boolean] =
    {
        (id, events, expectedVersion) =>

        val clazz = getClassName
        val eventsMap = getEventsMap(clazz)
        val eventsSeq = getEventsSeq(id, eventsMap)

        val newEventsSeq = eventsSeq ++ events
        val newEventsMap = eventsMap.updated(id, newEventsSeq)
        aggregatesEventsMap = aggregatesEventsMap.updated(clazz, newEventsMap)

        Future.successful(true)
    }

    private def getClassName[T: Manifest]: String =
    {
        implicitly[Manifest[T]].getClass.getName
    }

    private def getEventsMap(clazz: String): Map[AggregateId, Seq[AnyRef]] =
    {
        aggregatesEventsMap.getOrElse(clazz, Map.empty)
    }

    private def getEventsSeq(id: AggregateId, eventsMap: Map[AggregateId, Seq[AnyRef]]): Seq[AnyRef] =
    {
        eventsMap.getOrElse(id, Seq.empty)
    }
}
