package com.pointswarm.minions.aggregator

import com.pointswarm.commands._
import com.pointswarm.fireLegion._
import com.scalasourcing.backend.EventStorage
import com.scalasourcing.model.Aggregate.{Factory, CommandOf, IdOf}
import com.scalasourcing.model.AggregateRoot
import org.json4s.Formats

import scala.concurrent._

class Aggregator(eventStorage: EventStorage)(implicit f: Formats, ec: ExecutionContext) extends Minion[DoAggregateCommand]
{
    private var commandsMap: Map[String, (DoAggregateCommand) => Future[AnyRef]] = Map.empty

    def execute(commandId: CommandId, command: DoAggregateCommand): Future[AnyRef] =
    {
        val rootName = command.id.getClass.getName.split('.').last.split('$').head
        commandsMap(rootName)(command)
    }

    def register[Root <: AggregateRoot[Root] : Factory : Manifest](): Unit =
    {
        val name = implicitly[Manifest[Root]].runtimeClass.getName.split('.').last

        commandsMap = commandsMap.updated(name, (c) =>
        {
            eventStorage.execute(c.id.asInstanceOf[IdOf[Root]], c.payload.asInstanceOf[CommandOf[Root]])
        })
    }
}