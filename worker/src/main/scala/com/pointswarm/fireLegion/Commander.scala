package com.pointswarm.fireLegion

import java.lang.System.err

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.dmitryegorov.hellfire.Hellfire._
import com.dmitryegorov.tools.extensions.ThrowableExtensions._
import com.firebase.client._
import com.pointswarm.fireLegion.interfaces.Conqueror
import org.json4s.Formats

import scala.concurrent._
import scala.util._

class Commander[TCommand <: AnyRef : Manifest]
(root: Firebase, minion: Minion[TCommand])
(implicit f: Formats, ec: ExecutionContext)
    extends Conqueror {
    private lazy val commandName = CommandName[TCommand]
    private lazy val minionName = MinionName(minion)

    private lazy val minionRoot = root / "minions" / minionName
    private lazy val inboxRoot = minionRoot / "inbox"
    private lazy val resultsRoot = minionRoot / "results"
    private lazy val minionMapKeyRoot = root / "minionsMap" / minionName

    def prepare: Future[Unit] = {
        val prepare = minion.prepare
        val distribution = subscribeForDistribution

        List(prepare, distribution).waitAll
    }

    def conquer(completeWith: CancellationToken): Future[Int] = {
        val minionsRun = minion.conquer(completeWith)

        val myRun =
            inboxRoot
            .observeAdded
            .completeWith(completeWith)
            .concatMapF(x => execute(x.ds))
            .countF

        List(minionsRun, myRun).whenAll.map(l => l.drop(1).head)
    }

    private def execute(ds: DataSnapshot): Future[Unit] = {
        val id = CommandId(ds.getKey)
        val commandTry = Try(ds.value[TCommand].get)

        val responseFuture =
            for
            {
                c <- commandTry.asFuture
                _ = logCommandReceived(id, c)

                r <- minion.execute(id, c).recoverAsTry

                _ <- saveResult(id, c, r)
                _ <- removeCommand(id)
            } yield r

        responseFuture
        .flatRecoverAsTry
        .map(response => logExecuted(id, response))
    }


    private def removeCommand(commandId: CommandId): Future[String] =
        inboxRoot / commandId remove

    private def saveResult(commandId: CommandId, command: TCommand, responseTry: Try[AnyRef]): Future[String] =
        resultsRoot / commandId <-- CommandResult(responseTry, command)

    private def subscribeForDistribution: Future[String] =
        minionMapKeyRoot <-- commandName

    private def logCommandReceived(commandId: CommandId, command: TCommand) {
        println(s"Command recieved by $minionName: $commandId, $command")
    }

    private def logExecuted(commandId: CommandId, result: Try[AnyRef]) {
        result match
        {
            case Success(response)  =>
                println(s"Command $commandName '$commandId' executed by $minionName: $response")
            case Failure(exception) =>
                err.println(s"Command $commandName '$commandId' failed by $minionName: ${exception.fullMessage }")
        }
    }
}