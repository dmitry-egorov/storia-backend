package com.pointswarm.tools.fireLegion.distributor

import com.firebase.client.Firebase
import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.hellfire.Extensions._
import com.pointswarm.tools.fireLegion._
import org.json4s._

import scala.concurrent._

class Distributor(root: Firebase)(implicit ec: ExecutionContext, f: Formats) extends Minion[DistributeCommand]
{
    private val mapper = new CommandsMapper(root / "minionsMap")

    override def prepare: Future[Unit] =
    {
        mapper.prepare
    }

    override def conquer(completeWith: CancellationToken): Future[Int] = mapper.run(completeWith)

    def execute(id: CommandId, command: DistributeCommand): Future[DistributedResponse] =
    {
        val currentMap = mapper.getCurrentMap

        val name = command.name
        val payload = command.payload

        val minions = currentMap.getOrElse(name, Nil)

        if (minions.isEmpty) throw MinionsNotFoundException(name)

        minions
        .map(m => send(id, payload, m))
        .whenAll
        .map(_ => DistributedResponse(minions))
    }

    private def send(id: CommandId, command: AnyRef, name: MinionName): Future[String] =
    {
        root / "minions" / name / "inbox" / id <-- command
    }
}


