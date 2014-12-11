package com.pointswarm.tools.processing

import com.firebase.client.Firebase
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.processing.interfaces.Summoner
import org.json4s.Formats

import scala.concurrent._

object Master
{
    def apply(): Master = new Master(Map.empty)
}

class Master(commanders: Map[Class[_], Summoner])
{
    def createArmy(commandsRef: Firebase, token: CancellationToken)(implicit ec: ExecutionContext, f: Formats): Army =
    {
        val generals = commanders.values.map(c => c.summonConqueror(commandsRef, token)).toList

        new Army(generals)
    }

    def recruit[TCommand <: AnyRef](minion: Minion[TCommand])(implicit m: Manifest[TCommand]): Master =
    {
        val clazz = m.runtimeClass

        val commander = commanders.getOrElse(clazz, Recruiter[TCommand]()).asInstanceOf[Recruiter[TCommand]]

        val newCommander = commander.recruit(minion)

        val newCommanders = commanders.updated(clazz, newCommander)

        new Master(newCommanders)
    }
}
