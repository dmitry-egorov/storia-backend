package com.pointswarm.fireLegion.distributor

import com.firebase.client._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.dmitryegorov.hellfire.Hellfire._
import com.dmitryegorov.hellfire._
import com.pointswarm.fireLegion._

import scala.collection.JavaConversions._
import scala.concurrent._


class CommandsMapper(commandsMapRef: Firebase)(implicit ec: ExecutionContext)
{
    private var map: Map[CommandName, List[MinionName]] = Map.empty

    def getCurrentMap: Map[CommandName, List[MinionName]] = map

    def prepare: Future[Unit] =
    {
        commandsMapRef
        .current
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



