package com.scalasourcing.backend

import com.scalasourcing.model._

import scala.concurrent.{ExecutionContext, Future}

trait EventStorage
{
    implicit val ec: ExecutionContext

    def get[R <: AggregateRoot[R]](a: Aggregate[R]): (a.Id) => Future[a.EventsSeq]
    def tryPersist[R <: AggregateRoot[R]](a: Aggregate[R]): (a.Id, a.EventsSeq, Int) => Future[Boolean]

    def persist[R <: AggregateRoot[R]](a: Aggregate[R])(id: a.Id, events: a.EventsSeq, expectedVersion: Int): Future[Unit] =
    {
        tryPersist(a)(id, events, expectedVersion)
        .flatMap(
                committed =>
                    if (committed) Future.successful(())
                    else persist(a)(id, events, expectedVersion)
            )
    }

    def execute[R <: AggregateRoot[R]](a: Aggregate[R])(id: a.Id, command: a.Command): Future[a.Result] =
    {
        tryExecute(a)(id, command)
        .flatMap(
                result =>
                    if (result.isDefined) Future.successful(result.get)
                    else execute(a)(id, command)
            )
    }

    def tryExecute[R <: AggregateRoot[R]](a: Aggregate[R])(id: a.Id, command: a.Command): Future[Option[a.Result]] =
    {
        implicit val f = a
        for
        {
            events <- get(a)(id)
            result = events ! command
            persisted <- result match
            {
                case Left(newEvents) => tryPersist(a)(id, newEvents, events.length)
                case _               => Future.successful(true)
            }
        }
        yield if (persisted) Some(result) else None
    }
}