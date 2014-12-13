package com.pointswarm.tools.fireLegion.distributor

import com.firebase.client.Firebase
import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.hellfire.Extensions._
import com.pointswarm.tools.fireLegion._
import org.json4s._

import scala.concurrent._

class Distributor(fb: Firebase)(implicit ec: ExecutionContext, f: Formats) extends Minion[DistributeCommand]
{
    private val mapper = new CommandsMapper(fb.child("minionsMap"))

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

        if (minions.isEmpty) throw new MinionsNotFoundException(name)

        minions
        .map(m => send(id, payload, m))
        .whenAll
        .map(_ => new DistributedResponse(name, payload, command.addedOn, minions))
    }

    private def send(id: CommandId, command: AnyRef, name: MinionName): Future[String] =
    {
        fb
        .child("minions")
        .child(name)
        .child("inbox")
        .child(id)
        .set(command)
    }
}


