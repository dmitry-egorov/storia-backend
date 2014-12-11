package com.pointswarm.tools.processing

import com.firebase.client.Firebase
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import org.json4s.Formats

import scala.concurrent._

object FireMaster
{
    def create(): FireMaster = new FireMaster(Map.empty)
}

class FireMaster(commanders: Map[Class[_], FireCommander])
{
    def run(commandsRef:Firebase, token: CancellationToken)(implicit ec: ExecutionContext, f: Formats): Future[Int] =
    {
        Future.sequence(commanders.values.map(c => c.run(commandsRef, token))).map(x => x.sum)
    }

    def subdue[TCommand <: AnyRef](minion: Minion[TCommand])(implicit m: Manifest[TCommand]): FireMaster =
    {
        val clazz = m.runtimeClass

        val v = commanders.getOrElse(clazz, SimpletonFireCommander.create[TCommand]())

        val commander = v.asInstanceOf[SimpletonFireCommander[TCommand]]

        val newCommander = commander.subdue(minion)

        val newCommanders = commanders.updated(clazz, newCommander)

        new FireMaster(newCommanders)
    }

}
