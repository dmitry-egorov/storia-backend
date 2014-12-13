package com.pointswarm.tools.fireLegion

import java.lang.System.err

import com.firebase.client._
import com.pointswarm.tools.extensions.StringExtensions._
import com.pointswarm.tools.extensions.ThrowableExtensions._
import com.pointswarm.tools.fireLegion.interfaces.Conqueror
import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.futuristic.ObservableExtensions._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.hellfire.Extensions._
import org.json4s.Formats

import scala.concurrent._
import scala.util._

class Commander[TCommand <: AnyRef]
(fb: Firebase, minion: Minion[TCommand])
(implicit m: Manifest[TCommand], f: Formats, ec: ExecutionContext)
    extends Conqueror
{
    private lazy val commandName = CommandName.of[TCommand]
    private lazy val minionName = getMinionName

    private lazy val minionRef = fb.child("minions").child(minionName)
    private lazy val inboxRef = minionRef.child("inbox")
    private lazy val resultsRef = minionRef.child("results")

    def prepare: Future[Unit] =
    {
        val prepare = minion.prepare
        val distribution = subscribeForDistribution

        List(prepare, distribution).waitAll
    }

    def conquer(completeWith: CancellationToken): Future[Int] =
    {
        val minionsRun = minion.conquer(completeWith)

        val myRun =
            inboxRef
            .observeAdded
            .completeWith(completeWith)
            .concatMapF(x => execute(x.ds))
            .countF

        List(minionsRun, myRun).whenAll.map(l => l.drop(1).head)
    }

    private def execute(ds: DataSnapshot): Future[Unit] =
    {
        val id = new CommandId(ds.getKey)
        val commandTry = Try(ds.value[TCommand].get)

        val responseFuture =
        for
        {
            command <- commandTry.asFuture
            responseTry <- minion.execute(id, command).recoverAsTry
            _ <- saveResponse(id, responseTry)
            _ <- removeCommand(id)
        } yield responseTry

        responseFuture
        .flatRecoverAsTry
        .map(response => logExecuted(id, response))
    }


    private def removeCommand(commandId: CommandId): Future[String] =
    {
        inboxRef(commandId) remove()
    }

    private def saveResponse(commandId: CommandId, responseTry: Try[AnyRef]): Future[String] =
    {
        val result = CommandResult(responseTry)

        resultRef(commandId) set result
    }

    def subscribeForDistribution: Future[String] =
    {
        fb
        .child("minionsMap")
        .child(minionName)
        .set(commandName.value)
    }

    private def inboxRef(commandId: CommandId): Firebase =
    {
        inboxRef.child(commandId)
    }

    private def resultRef(commandId: CommandId): Firebase =
    {
        resultsRef.child(commandId)
    }

    private def logCommandReceived(commandId: CommandId, command: TCommand)
    {
        println(s"Command recieved by $minionName: $commandId, $command")
    }

    private def logExecuted(commandId: CommandId, result: Try[AnyRef])
    {
        result match
        {
            case Success(response)  => println(s"Command $commandName '$commandId' executed by $minionName: $response")
            case Failure(exception) =>
                val message = exception.fullMessage
                err.println(s"Command $commandName '$commandId' failed by $minionName: $message")
        }
    }


    def getMinionName: MinionName =
    {
        minion.getClass.getSimpleName.decapitalize
    }
}