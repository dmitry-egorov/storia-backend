package com.scalasourcing.backend.memory

import com.scalasourcing.backend._
import com.scalasourcing.model.Aggregate._

import scala.concurrent.{ExecutionContext, Future}

class SingleThreadInMemoryEventStorage(implicit val ec : ExecutionContext) extends EventStorage with EventSource
{
    private var aggregatesEventsMap: Map[String, Map[AggregateId, Seq[AnyRef]]] = Map.empty
    private var subscribersMap: Map[String, Seq[AnyRef => Unit]] = Map.empty

    def get[AR: Manifest](id: IdOf[AR]): Future[EventsSeqOf[AR]] =
    {
        val clazz = getClassName
        val eventsMap = getEventsMap(clazz)
        val events = getEventsSeq(id, eventsMap)

        Future.successful(events.asInstanceOf[EventsSeqOf[AR]])
    }

    def tryPersist[AR: Manifest](id: IdOf[AR], events: EventsSeqOf[AR], expectedVersion: Int): Future[Boolean] =
    {
        val clazz = getClassName
        val eventsMap = getEventsMap(clazz)
        val eventsSeq = getEventsSeq(id, eventsMap)

        val newEventsSeq = eventsSeq ++ events
        val newEventsMap = eventsMap.updated(id, newEventsSeq)
        aggregatesEventsMap = aggregatesEventsMap.updated(clazz, newEventsMap)

        subscribersMap
        .get(clazz)
        .map(subs => events.map(e => subs.foreach(s => s(e))))

        Future.successful(true)
    }

    def subscribe[AR: Manifest](f: EventOf[AR] => Unit): () => Unit =
    {
        val clazz = getClassName
        val callback: (AnyRef) => Unit = e => f(e.asInstanceOf[EventOf[AR]])

        val subs = subscribersMap.getOrElse(clazz, Seq.empty)
        val newSubs = subs ++ Seq(callback)
        subscribersMap = subscribersMap.updated(clazz, newSubs)

        () =>
        {
            val subs = subscribersMap.getOrElse(clazz, Seq.empty)
            val newSubs = subs.filter(i => i != callback)
            subscribersMap = subscribersMap.updated(clazz, newSubs)
        }
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
