package com.pointswarm.tools.processing

import java.lang.System.err

import com.firebase.client._
import com.pointswarm.tools.extensions.FirebaseExtensions._
import com.pointswarm.tools.extensions.StringExtensions._
import com.pointswarm.tools.extensions.ThrowableExtensions.ThrowableEx
import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.futuristic.ObservableExtensions._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.processing.interfaces.Conqueror
import org.json4s.Formats

import scala.async.Async._
import scala.concurrent._
import scala.util._

class Commander[TCommand <: AnyRef](commandsRef: Firebase, commandName: String, minions: List[Minion[TCommand]], completeWith: CancellationToken)(implicit m: Manifest[TCommand], f: Formats, ec: ExecutionContext)
    extends Conqueror
{

    private lazy val commandRef = commandsRef.child(commandName)
    private lazy val queueRef = commandRef.child("queue")
    private lazy val processedRef = commandRef.child("processed")
    private lazy val resultsRef = commandRef.child("results")

    def prepare: Future[Unit] = minions.map(_.prepare).whenAll.map(_ => ())

    def conquer: Future[Int] =
    {
        queueRef
        .observeAdded
        .completeWith(completeWith)
        .concatMapF(x => execute(x.ds))
        .countF()
    }

    private def execute(ds: DataSnapshot): Future[Unit] =
    {
        val id = new CommandId(ds.getKey)

        Try(ds.value[TCommand]) match
        {
            case Success(command) =>
                logCommandReceived(id, command)
                orderAll(id, command)
            case Failure(cause)   =>
                logFailedToParseCommand(id, cause)
                Future.successful(())
        }
    }

    private def orderAll(id: CommandId, command: TCommand): Future[Unit] =
        async
        {
            val allFuture =
                minions
                .map(m => order(m, id, command))
                .whenAll

            val all = await(allFuture)

            if (all.forall(x => x))
            {
                val doneFuture =
                    async
                    {
                        await(saveProcessed(id, command))
                        await(removeCommand(id))
                    }
                    .recoverAsTry

                val done = await(doneFuture)

                logExecuted(id, done)
            }
            else
            {
                logFailed(id)
            }
        }

    private def order(minion: Minion[TCommand], id: CommandId, command: TCommand): Future[Boolean] =
        async
        {
            val result = await(minion.execute(command).recoverAsTry)
            val name = minion.name.decapitalize

            val response = Response(result)

            val save = saveResponse(name, id, response)
                       .map(_ => result)
                       .flatRecoverAsTry

            val endResult = await(save)

            logExecuted(name, id, endResult)

            result.isSuccess
        }

    private def logFailedToParseCommand(commandId: CommandId, throwable: Throwable) =
    {
        err.println(s"Failed to parse command '$commandId': ${throwable.fullMessage }")
    }

    private def logCommandReceived(commandId: CommandId, command: TCommand)
    {
        println(s"Received command: $commandId, $command")
    }

    private def removeCommand(commandId: CommandId): Future[Unit] =
    {
        queuedRef(commandId) remove()
    }

    private def queuedRef(commandId: CommandId): Firebase =
    {
        queueRef.child(commandId.value)
    }

    private def saveProcessed(commandId: CommandId, command: TCommand): Future[Unit] =
    {
        processedRef(commandId) set command
    }

    private def processedRef(commandId: CommandId): Firebase =
    {
        processedRef.child(commandId.value)
    }

    private def saveResponse(minionName: String, commandId: CommandId, result: AnyRef): Future[Unit] =
    {
        resultRef(minionName, commandId) set result
    }

    private def resultRef(minionName: String, commandId: CommandId): Firebase =
    {
        resultsRef.child(commandId.value).child(minionName)
    }

    private def logExecuted(minionName: String, commandId: CommandId, result: Try[AnyRef])
    {
        result match
        {
            case Success(response)  => println(s"Command $commandName '$commandId' executed by $minionName: $response")
            case Failure(exception) =>
                val message = exception.fullMessage
                err.println(s"Command $commandName '$commandId' failed by $minionName: $message")
        }
    }

    private def logExecuted(commandId: CommandId, result: Try[Unit])
    {
        result match
        {
            case Success(_)         => println(s"Command $commandName '$commandId' executed.")
            case Failure(exception) =>
                val message = exception.fullMessage
                err.println(s"Command $commandName '$commandId' failed: $message")
        }
    }

    private def logFailed(commandId: CommandId)
    {
        err.println(s"Command $commandName '$commandId' failed.")
    }
}