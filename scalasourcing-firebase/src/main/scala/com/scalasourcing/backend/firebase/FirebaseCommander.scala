package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client._
import com.scalasourcing.backend._
import com.scalasourcing.model.Aggregate
import com.scalasourcing.model.Aggregate.AggregateResult
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

abstract class FirebaseCommander(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Executor[AggregateResult]
{
    protected val ag: Aggregate
    protected val es: EventStorage[ag.type]
    protected implicit val mi: Manifest[ag.Id]
    protected implicit val mc: Manifest[ag.Command]

    lazy val rootRef = fb / "commands" / ag.name
    lazy val inboxRef = rootRef / "inbox"
    lazy val resultsRef = rootRef / "results"

    def run(completeWith: CancellationToken): Observable[Try[ag.Result]] =
    {
        inboxRef
        .observeAdded
        .completeWith(completeWith)
        .concatMapF(x => execute(x))
    }

    def prepare(completeWith: CancellationToken) = Future.successful(())

    private def execute(snapshot: DataSnapshot): Future[Try[ag.Result]] =
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

    private def execute(id: CommandId, command: AggregateCommand[ag.Id, ag.Command]): Future[ag.Result] =
    {
        val payload = command.payload
        for
        {
            result <- es.execute(command.id, payload)
            _ <- writeResult(id, payload, result)
            _ <- removeCommand(id)
        }
        yield result
    }

    def writeResult(id: CommandId, command: ag.Command, result: ag.Result) =
    {
        resultsRef / id <-- CommandResult(command, result)
    }

    private def removeCommand(id: CommandId) =
    {
        inboxRef / id remove
    }
}
