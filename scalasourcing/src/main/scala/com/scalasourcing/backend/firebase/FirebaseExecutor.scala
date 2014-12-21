package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.{DataSnapshot, Firebase}
import com.scalasourcing.backend.firebase.StringExtensions._
import com.scalasourcing.backend.{EventStorage, ExecuteCommand, Executor}
import com.scalasourcing.model.Aggregate._
import com.scalasourcing.model.AggregateRoot
import org.json4s.Formats
import rx.lang.scala.Observable

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Try}


class FirebaseExecutor[Id <: IdOf[Root] : Manifest, Root <: AggregateRoot[Root] : Manifest : Factory]
(fb: Firebase, es: EventStorage)
(implicit f: Formats, ec: ExecutionContext)
    extends Executor
{
    lazy val rootRef = fb / "commands" / getRootName
    lazy val inboxRef = rootRef / "inbox"
    lazy val resultsRef = rootRef / "results"

    def run(completeWith: CancellationToken): Observable[Try[CommandResultOf[Root]]] =
    {
        inboxRef
        .observeAdded
        .completeWith(completeWith)
        .concatMapF(x => execute(x))
    }

    def execute(snapshot: DataSnapshot): Future[Try[CommandResultOf[Root]]] =
    {
        try
        {
            {
                execute(snapshot.getKey, snapshot.value[ExecuteCommand[Id, Root]].get).recoverAsTry
            }
        }
        catch
            {
                case e: Throwable => Future.successful(Failure(e))
            }
    }

    private def execute(id: String, command: ExecuteCommand[Id, Root]): Future[CommandResultOf[Root]] =
    {
        for
        {
            result <- es.execute(command.id, command.payload)
            _ <- writeResult(id, result)
            _ <- removeCommand(id)
        }
        yield result
    }

    def writeResult(id: String, result: CommandResultOf[Root]) =
    {
        resultsRef / id <-- result
    }

    private def removeCommand(id: String) =
    {
        inboxRef / id remove
    }

    private def getRootName = implicitly[Manifest[Root]].runtimeClass.getSimpleName.decapitalize
}
