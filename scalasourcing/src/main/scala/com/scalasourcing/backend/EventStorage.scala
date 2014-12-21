package com.scalasourcing.backend

import com.scalasourcing.model.Aggregate._
import com.scalasourcing.model._

import scala.concurrent._

trait EventStorage
{
    implicit val ec: ExecutionContext

    def get[AR: Manifest](id: IdOf[AR]): Future[EventsSeqOf[AR]]
    def tryPersist[AR: Manifest](id: IdOf[AR], events: EventsSeqOf[AR], expectedVersion: Int): Future[Boolean]

    def persist[AR: Manifest](id: IdOf[AR], events: EventsSeqOf[AR], expectedVersion: Int): Future[Unit] =
    {
        tryPersist(id, events, expectedVersion)
        .flatMap(
                committed =>
                    if (committed) Future.successful(())
                    else persist(id, events, expectedVersion)
            )
    }

    def execute[AR <: AggregateRoot[AR] : Factory : Manifest](id: IdOf[AR], command: CommandOf[AR]): Future[CommandResultOf[AR]] =
    {
        tryExecute(id, command)
        .flatMap(
                result =>
                    if (result.isDefined) Future.successful(result.get)
                    else execute(id, command)
            )
    }

    def tryExecute[AR <: AggregateRoot[AR] : Factory : Manifest](id: IdOf[AR], command: CommandOf[AR]): Future[Option[CommandResultOf[AR]]] =
    {
        for
        {
            events <- get(id)
            result = events ! command
            persisted <- result match
            {
                case Left(newEvents) => tryPersist(id, newEvents, events.length)
                case _               => Future.successful(true)
            }
        }
        yield if (persisted) Some(result) else None
    }
}