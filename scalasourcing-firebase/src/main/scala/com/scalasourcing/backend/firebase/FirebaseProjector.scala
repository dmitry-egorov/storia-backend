package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation._
import com.dmitryegorov.hellfire.Hellfire._
import com.dmitryegorov.hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.{Executor, ProjectionExecutionResult}
import com.scalasourcing.model._
import org.json4s.Formats
import rx.lang.scala.Observable

import scala.collection.concurrent.TrieMap
import scala.concurrent._
import scala.util._

object FirebaseProjector
{
    def apply(ag: Aggregate)(fb: Firebase, projection: Projection[ag.type])(implicit ec: ExecutionContext, f: Formats, mmi: Manifest[ag.Id], mme: Manifest[ag.Event]) =
    {
        new FirebaseProjector(fb)
        {
            protected override val a: ag.type = ag
            protected override val pr = projection
            protected override val mi = mmi
            protected override val me = mme
        }
    }
}

abstract class FirebaseProjector(fb: Firebase)(implicit ec: ExecutionContext, f: Formats) extends Executor
{
    protected val a: Aggregate
    protected val pr: Projection[a.type]
    protected implicit val mi: Manifest[a.Id]
    protected implicit val me: Manifest[a.Event]

    private val subscriptionsMap: TrieMap[String, CancellationSource] = TrieMap.empty

    private lazy val aggregatesEventsRef = fb / "aggregateEvents" / a.name
    private lazy val aggregatesIdsRef = fb / "aggregateIds" / a.name
    private lazy val projectionsVersionsRef = fb / "projectionVersions" / pr.name
    private def lastVersionRefOf(id: a.Id): Firebase = projectionsVersionsRef / id.hash
    private def aggregateEventsRef(id: a.Id): Firebase = aggregatesEventsRef / id.hash

    override def run(completeWith: CancellationToken): Observable[Try[ProjectionExecutionResult[a.type]]] =
    {
        aggregatesIdsRef
        .observeAddedData[a.Id]
        .completeWith(completeWith)
        .flatMap
        {
            case Success(DataAdded(hash, id)) => subscribe(completeWith, id)
            case Failure(error) => Observable.just(Failure(error))
        }
    }

    override def prepare(completeWith: CancellationToken): Future[Unit] = pr.prepare()

    private def subscribe(globalComplete: CancellationToken, aggId: a.Id): Observable[Try[ProjectionExecutionResult[a.type]]] =
    {
        val localComplete = addSubscription(aggId.hash)

        val complete = globalComplete + localComplete

        lastVersionRefOf(aggId)
        .value[Int]
        .observe
        .map(v => v.getOrElse(0))
        .flatMap(lastVersion => consume(complete, aggId, lastVersion))
    }

    private def consume(complete: CancellationToken, aggId: a.Id, last: Int): Observable[Try[ProjectionExecutionResult[a.type]]] =
    {
        aggregateEventsRef(aggId)
        .orderByKey()
        .startAt(last.toString)
        .observeAddedData[a.Event]
        .completeWith(complete)
        .flatMapF
        {
            case Success(added) => consume(aggId, added.value, added.id.toInt)
            case Failure(error) => Future.successful(Failure[ProjectionExecutionResult[a.type]](error))
        }
    }

    private def consume(aggId: a.Id, event: a.Event, eventId: Int): Future[Try[ProjectionExecutionResult[a.type]]] =
    {
        for
        {
            r <- pr.project(aggId, event, eventId).map(ProjectionExecutionResult[a.type](aggId, event, pr.name, _)).recoverAsTry
            //Todo: what will happen if events are lost?
            _ <- lastVersionRefOf(aggId).transaction[Int](x => if (x.getOrElse(-1) <= eventId) Some(eventId + 1)
            else None)
        } yield r
    }

    private def addSubscription(idHash: String): CancellationSource =
    {
        val cancellation = new CancellationSource
        subscriptionsMap(idHash) = cancellation
        cancellation
    }

    private def cancelSubscription(id: String): Observable[Try[AnyRef]] =
    {
        subscriptionsMap(id).cancel()
        subscriptionsMap.remove(id)

        Observable.just(Success("Removed subscription"))
    }
}


