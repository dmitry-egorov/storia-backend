package com.pointswarm.projections

import com.scalasourcing.model.Aggregate.{IdOf, EventOf}

import scala.concurrent.Future

trait Projection[Id <: IdOf[Root], Root]
{
    def consume(id: Id, event: EventOf[Root]): Future[Unit]
}
