package com.scalasourcing.backend

import com.scalasourcing.model._

import scala.concurrent.{ExecutionContext, Future}

abstract class EventStorage[A <: Aggregate](protected val a: A)(implicit val ec: ExecutionContext)
{
    def get(id: a.Id): Future[a.EventsSeq]
    def tryPersist(id: a.Id, events: a.EventsSeq, expectedVersion: Int): Future[Boolean]

    def persist(id: a.Id, events: a.EventsSeq, expectedVersion: Int): Future[Unit] =
    {
        tryPersist(id, events, expectedVersion)
        .flatMap(
                committed =>
                    if (committed) Future.successful(())
                    else persist(id, events, expectedVersion)
            )
    }

    def execute(id: a.Id, command: a.Command): Future[a.Result] =
    {
        tryExecute(id, command)
        .flatMap(
                result =>
                    if (result.isDefined) Future.successful(result.get)
                    else execute(id, command)
            )
    }

    def tryExecute(id: a.Id, command: a.Command): Future[Option[a.Result]] =
    {
        for
        {
            events <- get(id)
            result = a.seed + events ! command
            persisted <- result match
            {
                case Left(newEvents) => tryPersist(id, newEvents, events.length)
                case _               => Future.successful(true)
            }
        }
        yield if (persisted) Some(result) else None
    }
}