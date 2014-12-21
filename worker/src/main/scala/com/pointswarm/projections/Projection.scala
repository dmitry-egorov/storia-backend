package com.pointswarm.projections

import com.scalasourcing.model.Aggregate.{AggregateId, EventOf}

import scala.concurrent.Future

trait Projection[Id <: AggregateId, Root]
{
    def consume(id: Id, event: EventOf[Root]): Future[Unit]
}
