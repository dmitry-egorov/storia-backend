package com.pointswarm.tools.processing

import java.lang.System.err

import com.firebase.client._
import com.pointswarm.tools.extensions.StringExtensions._
import com.pointswarm.tools.extensions.ThrowableExtensions._
import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.futuristic.ObservableExtensions._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.hellfire.Extensions._
import com.pointswarm.tools.processing.interfaces.Conqueror
import org.json4s.Formats

import scala.async.Async._
import scala.concurrent._
import scala.util._

class Commander[TCommand <: AnyRef]
(fb: Firebase, minion: Minion[TCommand])
(implicit m: Manifest[TCommand], f: Formats, ec: ExecutionContext)
    extends Conqueror
{
    private lazy val commandName = getCommandName
    private lazy val minionName = getMinionName

    private lazy val minionRef = fb.child("minions").child(minionName)
    private lazy val inboxRef = minionRef.child("inbox")
    private lazy val resultsRef = minionRef.child("results")

    def prepare: Future[Unit] =
    {
        val prepare = minion.prepare
        val subscribe = subscribeForDistribution

        async
        {
            await(prepare)
            await(subscribe)
        }
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

        async
        {
            await(minionsRun)
            await(myRun)
        }
    }

    private def execute(ds: DataSnapshot): Future[Unit] =
        async
        {
            val id = new CommandId(ds.getKey)

            val responseFuture =
                async
                {
                    val command = await(Try(ds.value[TCommand]).asFuture)

                    logCommandReceived(id, command)

                    val responseTry = await(minion.execute(id, command).recoverAsTry)

                    await(saveResponse(id, responseTry))
                    await(removeCommand(id))

                    responseTry
                }
                .flatRecoverAsTry

            val response = await(responseFuture)

            logExecuted(id, response)
        }


    private def removeCommand(commandId: CommandId): Future[Unit] =
    {
        inboxRef(commandId) remove()
    }

    private def saveResponse(commandId: CommandId, responseTry: Try[AnyRef]): Future[Unit] =
    {
        val result = Result(responseTry)

        resultRef(commandId) set result
    }

    def subscribeForDistribution: Future[Unit] =
    {
        fb
        .child("minionsMap")
        .child(minionName)
        .set(commandName)
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

    private def getCommandName: CommandName =
    {
        m
        .runtimeClass
        .getSimpleName
        .replaceAll("Command", "")
        .decapitalize
    }

    def getMinionName: MinionName =
    {
        minion.getClass.getSimpleName.decapitalize
    }
}