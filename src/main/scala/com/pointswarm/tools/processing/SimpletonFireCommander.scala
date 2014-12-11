package com.pointswarm.tools.processing

import com.firebase.client.Firebase
import com.pointswarm.tools.extensions.ListExtensions._
import com.pointswarm.tools.extensions.StringExtensions._
import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import org.json4s.Formats

import scala.concurrent._
import scala.util.{Failure, Success, Try}

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

        FirebaseCommandProcessor.run(commandsRef, commandName, commandMinions, token)(m, f)
    }

    private def commandMinions(command: TCommand)(implicit ec: ExecutionContext): Future[AnyRef] =
    {
        val minionFutures = minions.map(m => commandMinion(command, m))

        Future
        .sequence(minionFutures)
        .map(l => l.asMap)
    }

    private def commandMinion(command: TCommand, minion: Minion[TCommand])(implicit ec: ExecutionContext): Future[(String, Response)] =
    {
        minion
        .obey(command)
        .recoverAsTry
        .map(responseFrom)
        .map(r => (minion.name.decapitalize, r))
    }

    private def getCommandName: String =
    {
        m.runtimeClass.getSimpleName.replaceAll("Command", "").decapitalize
    }

    private def responseFrom(result: Try[AnyRef]): Response =
    {
        result match
        {
            case Success(data)  => new Response(true, data, null)
            case Failure(cause) => new Response(false, Nil, cause.getMessage)
        }
    }
}


