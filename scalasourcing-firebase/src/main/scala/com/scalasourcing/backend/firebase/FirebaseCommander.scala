package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client._
import com.scalasourcing.backend._
import com.scalasourcing.model.Aggregate
import com.scalasourcing.model.Aggregate.AggregateResult
import org.joda.time.{DateTimeZone, DateTime}
import org.json4s.Formats
import rx.lang.scala.Observable

import scala.concurrent._
import scala.util._

object FirebaseCommander
{
    def apply(agg: Aggregate)(fb: Firebase, fes: EventStorage[agg.type])(implicit f: Formats, ec: ExecutionContext, mmi: Manifest[agg.Id], mmc: Manifest[agg.Command]) =
    {
        new FirebaseCommander(fb)
        {
            protected override val ag: agg.type = agg
            protected override val es = fes
            protected override val mi = mmi
            protected override val mc = mmc
        }
    }
}

abstract class FirebaseCommander(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Executor
{
    protected val ag: Aggregate
    protected val es: EventStorage[ag.type]
    protected implicit val mi: Manifest[ag.Id]
    protected implicit val mc: Manifest[ag.Command]

    lazy val rootRef = fb / "commands" / ag.name
    lazy val inboxRef = rootRef / "inbox"
    lazy val resultsRef = rootRef / "results"

    def run(completeWith: CancellationToken): Observable[Try[CommandExecutionResult[ag.type]]] =
    {
        inboxRef
        .observeAdded
        .completeWith(completeWith)
        .concatMapF(x => execute(x))
    }

    def prepare(completeWith: CancellationToken) = Future.successful(())

    private def execute(snapshot: DataSnapshot): Future[Try[CommandExecutionResult[ag.type]]] =
    {
        try
        {
            execute(snapshot.getKey, snapshot.value[AggregateCommand[ag.Id, ag.Command]].get).recoverAsTry
        }
        catch
            {
                case e: Throwable => Future.successful(Failure(e))
            }
    }

    private def execute(commandId: CommandId, command: AggregateCommand[ag.Id, ag.Command]): Future[CommandExecutionResult[ag.type]] =
    {
        val payload = command.payload
        val aggId = command.id
        val addedOn = command.addedOn
        for
        {
            result <- es.execute(aggId, payload)
            commandResult = CommandExecutionResult[ag.type](aggId, payload, result, addedOn, DateTime.now(DateTimeZone.UTC))
            _ <- writeResult(commandId, commandResult)
            _ <- removeCommand(commandId)
        }
        yield commandResult
    }

    def writeResult(id: CommandId, commandResult: CommandExecutionResult[ag.type]) =
    {
        resultsRef / id <-- commandResult
    }

    private def removeCommand(id: CommandId) =
    {
        inboxRef / id remove
    }
}
