package com.pointswarm.tools.processing

import com.firebase.client.Firebase
import com.pointswarm.tools.extensions.StringExtensions._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import org.json4s.Formats

import scala.concurrent._

trait FireCommander
{
    def run(commandsRef: Firebase, token: CancellationToken)(implicit ec: ExecutionContext, f: Formats): Future[Int]
}

object SimpletonFireCommander
{
    def create[TCommand <: AnyRef]()(implicit m: Manifest[TCommand]): SimpletonFireCommander[TCommand] =
    {
        new SimpletonFireCommander[TCommand](List.empty)
    }
}

class SimpletonFireCommander[TCommand <: AnyRef](minions: List[Minion[TCommand]])(implicit m: Manifest[TCommand]) extends FireCommander
{
    def subdue(minion: Minion[TCommand]): SimpletonFireCommander[TCommand] =
    {
        new SimpletonFireCommander(minion :: minions)
    }

    def run(commandsRef: Firebase, token: CancellationToken)(implicit ec: ExecutionContext, f: Formats): Future[Int] =
    {
        val commandName = getCommandName

        FirebaseCommandProcessor.run(commandsRef, commandName, minions, token)(m, f)
    }

    private def getCommandName: String =
    {
        m.runtimeClass.getSimpleName.replaceAll("Command", "").decapitalize
    }
}


