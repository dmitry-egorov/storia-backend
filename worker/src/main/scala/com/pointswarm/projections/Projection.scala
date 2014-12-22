package com.pointswarm.projections

import com.scalasourcing.model.{AggregateEvent, AggregateId}

import scala.concurrent.Future

trait Projection[Id <: AggregateId]
{
    def consume(id: Id, event: AggregateEvent): Future[Unit]
}
