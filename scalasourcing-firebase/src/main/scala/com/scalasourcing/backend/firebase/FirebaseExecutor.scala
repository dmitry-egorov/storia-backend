package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client._
import com.scalasourcing.backend._
import com.scalasourcing.backend.firebase.StringExtensions._
import com.scalasourcing.model.{Aggregate, AggregateRoot}
import org.json4s.Formats
import rx.lang.scala.Observable

import scala.concurrent._
import scala.util._

object FirebaseExecutor
{
    def apply[R <: AggregateRoot[R] : Aggregate]
    (a: Aggregate[R])(fb: Firebase, es: EventStorage)
    (implicit f: Formats, ec: ExecutionContext, m1: Manifest[a.Id], m2: Manifest[R]) = new Executor {

        lazy val rootRef = fb / "commands" / getRootName
        lazy val inboxRef = rootRef / "inbox"
        lazy val resultsRef = rootRef / "results"

        def run(completeWith: CancellationToken): Observable[Try[a.Result]] =
        {
            inboxRef
            .observeAdded
            .completeWith(completeWith)
            .concatMapF(x => execute(x))
        }

        def execute(snapshot: DataSnapshot): Future[Try[a.Result]] =
        {
            try
            {
                {
                    execute(snapshot.getKey, snapshot.value[ExecuteCommand[a.Id, R]].get).recoverAsTry
                }
            }
            catch
                {
                    case e: Throwable => Future.successful(Failure(e))
                }
        }

        private def execute(id: String, command: ExecuteCommand[a.Id, R]): Future[a.Result] =
        {
            for
            {
                result <- es.execute(a)(command.id, command.payload)
                _ <- writeResult(id, result)
                _ <- removeCommand(id)
            }
            yield result
        }

        def writeResult(id: String, result: a.Result) =
        {
            resultsRef / id <-- result
        }

        private def removeCommand(id: String) =
        {
            inboxRef / id remove
        }

        private def getRootName = m2.runtimeClass.getSimpleName.decapitalize
    }
}
