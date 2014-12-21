package com.pointswarm.minions.aggregator

import com.pointswarm.commands._
import com.pointswarm.fireLegion._
import com.scalasourcing.backend.EventStorage
import com.scalasourcing.model.Aggregate.{Factory, IdOf}
import com.scalasourcing.model.AggregateRoot
import org.json4s.Formats

import scala.concurrent._

abstract class Aggregator[Id <: IdOf[Root], Root <: AggregateRoot[Root] : Factory : Manifest](implicit f: Formats, ec: ExecutionContext) extends Minion[DoAggregateCommand[Id, Root]]
{
    def eventStorage: EventStorage
    def execute(commandId: CommandId, c: DoAggregateCommand[Id, Root]): Future[AnyRef] =
    {
        eventStorage.execute(c.id, c.payload)
    }
}