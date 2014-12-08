package com.pointswarm.processing

import com.firebase.client._
import com.pointswarm.extensions.Added
import com.pointswarm.extensions.FirebaseExtensions._
import com.pointswarm.extensions.FutureExtensions._
import com.pointswarm.extensions.ObservableExtensions.ObservableEx
import com.pointswarm.extensions.ThrowableExtensions.ThrowableEx
import org.json4s.Formats
import rx.lang.scala.Subscription

import scala.async.Async._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.util._

object FirebaseCommandProcessor
{
    def run[TCommand <: AnyRef](fb: Firebase, commandName: String, processor: TCommand => Future[Product])(implicit m: Manifest[TCommand], fmt: Formats): Subscription =
    {
        new FirebaseCommandProcessor[TCommand](fb, commandName)
        .run(processor)
    }
}

class FirebaseCommandProcessor[TCommand <: AnyRef](fb: Firebase, commandName: String)(implicit m: Manifest[TCommand], f: Formats)
{
    def run(process: TCommand => Future[Product]): Subscription =
    {
        fb
        .child("commands")
        .child(commandName)
        .observe
        .collect
        {
            case x: Added => x
        }
        .futureMap(x => processCommand(process, x.ds))
        .subscribe()
    }

    def processCommand(process: TCommand => Future[Product], x: DataSnapshot): Future[Unit] =
        async
        {
            val id = new CommandId(x.getKey)
            val command = x.value[TCommand]

            logCommandReceived(id, command)

            val result = await(process(command).recoverAsTry)

            val done =
                async
                {
                    await(saveResponse(id, result))
                    await(saveProcessed(id, command))
                    await(removeCommand(id))

                    result
                }.flatRecoverAsTry

            val complete = await(done)

            logProcessed(id, complete)
        }

    private def logCommandReceived(commandId: CommandId, command: TCommand)
    {
        println(s"Received command: $commandId, $command")
    }

    private def removeCommand(commandId: CommandId): Future[Unit] =
    {
        fb
        .child("commands")
        .child(commandName)
        .child(commandId.value)
        .remove()
    }

    private def saveProcessed(commandId: CommandId, command: TCommand): Future[Unit] =
    {
        fb
        .child("processedCommands")
        .child(commandName)
        .child(commandId.value)
        .set(command)
    }

    private def saveResponse(commandId: CommandId, result: Try[Product]): Future[Unit] =
    {
        val response = result match
        {
            case Success(data)  => new Response(true, data, null)
            case Failure(cause) => new Response(false, Nil, cause.getMessage)
        }

        fb
        .child("results")
        .child(commandName)
        .child(commandId.value)
        .set(response)
    }

    private def logProcessed(commandId: CommandId, result: Try[Product])
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