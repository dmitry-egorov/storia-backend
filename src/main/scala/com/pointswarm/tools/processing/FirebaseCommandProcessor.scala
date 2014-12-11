package com.pointswarm.tools.processing

import com.firebase.client._
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.futuristic._
import com.pointswarm.tools.extensions.FirebaseExtensions._
import FutureExtensions._
import ObservableExtensions._
import com.pointswarm.tools.extensions.ThrowableExtensions.ThrowableEx
import org.json4s.Formats

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util._

object FirebaseCommandProcessor
{
    def run[TCommand <: AnyRef](commandsRef: Firebase, commandName: String, processor: TCommand => Future[AnyRef], token: CancellationToken)(implicit m: Manifest[TCommand], fmt: Formats): Future[Int] =
    {
        new FirebaseCommandProcessor[TCommand](commandsRef, commandName)
        .run(processor, token)
    }
}

class FirebaseCommandProcessor[TCommand <: AnyRef](commandsRef: Firebase, commandName: String)(implicit m: Manifest[TCommand], f: Formats)
{

    private lazy val commandRef = commandsRef.child(commandName)
    private lazy val queueRef = commandRef.child("queue")
    private lazy val processedRef = commandRef.child("processed")
    private lazy val resultsRef = commandRef.child("results")

    def run(process: TCommand => Future[AnyRef], completeWith: CancellationToken): Future[Int] =
    {
        queueRef
        .observeAdded
        .completeWith(completeWith)
        .concatMapF(x => processCommand(process, x.ds))
        .countF()
    }

    private def processCommand(process: TCommand => Future[AnyRef], x: DataSnapshot): Future[Unit] =
    {
        val id = new CommandId(x.getKey)

        Try(x.value[TCommand]) match
        {
            case Success(command) =>
                processCommand(process, id, command)
            case Failure(cause)   =>
                logFailedToParseCommand(id, cause)
                Future.successful(())
        }
    }

    private def processCommand(process: TCommand => Future[AnyRef], id: CommandId, command: TCommand): Future[Unit] =
        async
        {
            logCommandReceived(id, command)

            val result = await(process(command))

            val done =
                async
                {
                    await(saveResponse(id, result))
                    await(saveProcessed(id, command))
                    await(removeCommand(id))

                    result
                }
                .recoverAsTry

            val complete = await(done)

            logProcessed(id, complete)
        }

    private def logFailedToParseCommand(commandId: CommandId, throwable: Throwable) =
    {
        println(s"Failed to parse command '$commandId': ${throwable.fullMessage }")
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

    private def saveResponse(commandId: CommandId, result: AnyRef): Future[Unit] =
    {
        resultRef(commandId) set result
    }

    private def resultRef(commandId: CommandId): Firebase =
    {
        resultsRef.child(commandId.value)
    }

    private def logProcessed(commandId: CommandId, result: Try[AnyRef])
    {
        result match
        {
            case Success(response)  => println(s"Command $commandName '$commandId' processed: $response")
            case Failure(exception) =>
                val message = exception.fullMessage
                println(s"Command $commandName '$commandId' failed: $message")
        }
    }
}