package com.pointswarm.minions.distributor

import com.firebase.client._
import com.pointswarm.tools.futuristic.ObservableExtensions._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.hellfire.Extensions._
import com.pointswarm.tools.hellfire.events._
import com.pointswarm.tools.processing._

import scala.collection.JavaConversions._
import scala.concurrent._


class CommandsMapper(commandsMapRef: Firebase)(implicit ec: ExecutionContext)
{
    private var map: Map[CommandName, List[MinionName]] = Map.empty

    def getCurrentMap: Map[CommandName, List[MinionName]] = map

    def prepare: Future[Unit] =
    {
        commandsMapRef
        .value
        .map(ds =>
             {
                 ds
                 .getChildren
                 .foreach(x => updateMinions(x, (l, m) => m :: l))
             })
    }

    def run(token: CancellationToken): Future[Int] =
    {
        commandsMapRef
        .observe
        .completeWith(token)
        .map(x => change(x))
        .countF
    }

    private def change(event: Event): Unit =
    {
        event match
        {
            case Added(ds)   => updateMinions(ds, (l, m) => if (l.contains(m)) l else m :: l)
            case Removed(ds) => updateMinions(ds, (l, m) => l.filter(_ != m))
            case Changed(ds) => updateMinions(ds, (l, m) => m :: l.filter(_ != m))
        }
    }

    def updateMinions(ds: DataSnapshot, f: (List[MinionName], MinionName) => List[MinionName])
    {
        val name = ds.getKey
        val commandName = ds.getValue.toString

        val minions = map.getOrElse(commandName, Nil)

        val newMinions = f(minions, name)

        map = map.updated(commandName, newMinions)
    }
}



