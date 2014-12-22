package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client._
import com.scalasourcing.backend._
import com.scalasourcing.backend.firebase.StringExtensions._
import com.scalasourcing.model.Aggregate
import org.json4s.Formats
import rx.lang.scala.Observable

import scala.concurrent._
import scala.util._

object FirebaseExecutor
{
    def apply
    (agg: Aggregate, fb: Firebase)
    (fes: EventStorage
        {val a: agg.type})
    (implicit f: Formats, ec: ExecutionContext, m20: Manifest[agg.type], m21: Manifest[agg.Id], m22: Manifest[agg.Command])
    : FirebaseExecutor
        {val ag: agg.type} =
    {
        new FirebaseExecutor(fb)
        {
            override val ag: agg.type = agg
            override val es = fes
            implicit val m = m20
            implicit val m1 = m21
            implicit val m2 = m22
        }
    }
}

abstract class FirebaseExecutor(val fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Executor
{
    val ag: Aggregate

    val es: EventStorage
        {
            val a: ag.type
        }

    implicit val m: Manifest[ag.type]
    implicit val m1: Manifest[ag.Id]
    implicit val m2: Manifest[ag.Command]


    lazy val rootRef = fb / "commands" / getRootName
    lazy val inboxRef = rootRef / "inbox"
    lazy val resultsRef = rootRef / "results"

    def run(completeWith: CancellationToken): Observable[Try[ag.Result]] =
    {
        inboxRef
        .observeAdded
        .completeWith(completeWith)
        .concatMapF(x => execute(x))
    }

    def execute(snapshot: DataSnapshot): Future[Try[ag.Result]] =
    {
        try
        {
            {
                execute(snapshot.getKey, snapshot.value[ExecuteCommand[ag.Id, ag.Command]].get).recoverAsTry
            }
        }
        catch
            {
                case e: Throwable => Future.successful(Failure(e))
            }
    }

    private def execute(id: String, command: ExecuteCommand[ag.Id, ag.Command]): Future[ag.Result] =
    {
        for
        {
            result <- es.execute(command.id, command.payload)
            _ <- writeResult(id, result)
            _ <- removeCommand(id)
        }
        yield result
    }

    def writeResult(id: String, result: ag.Result) =
    {
        resultsRef / id <-- result
    }

    private def removeCommand(id: String) =
    {
        inboxRef / id remove
    }

    private def getRootName =
    {
        m.runtimeClass.getSimpleName.replace("$", "").decapitalize
    }

}
