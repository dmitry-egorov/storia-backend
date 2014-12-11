package com.pointswarm.tools.processing

import com.firebase.client.Firebase
import com.pointswarm.tools.extensions.StringExtensions._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.processing.interfaces.{Conqueror, Summoner}
import org.json4s.Formats

import scala.concurrent._

object Recruiter
{
    def apply[TCommand <: AnyRef]()(implicit m: Manifest[TCommand]): Recruiter[TCommand] =
    {
        new Recruiter[TCommand](List.empty)
    }
}

case class Recruiter[TCommand <: AnyRef](minions: List[Minion[TCommand]])(implicit m: Manifest[TCommand]) extends Summoner
{
    def summonConqueror(commandsRef: Firebase, token: CancellationToken)(implicit ec: ExecutionContext, f: Formats): Conqueror =
    {
        val commandName = getCommandName

        new Commander(commandsRef, commandName, minions, token)
    }

    def recruit(minion: Minion[TCommand]): Recruiter[TCommand] =
    {
        copy(minion :: minions)
    }

    private def getCommandName: String =
    {
        m.runtimeClass.getSimpleName.replaceAll("Command", "").decapitalize
    }
}


