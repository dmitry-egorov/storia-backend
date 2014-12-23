package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation._
import com.dmitryegorov.hellfire.Hellfire._
import com.dmitryegorov.hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.Executor
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

abstract class FirebaseProjector(fb: Firebase)(implicit ec: ExecutionContext, f: Formats) extends Executor[AnyRef]
{
    protected val a: Aggregate
    protected val pr: Projection[a.type]
    protected implicit val mi: Manifest[a.Id]
    protected implicit val me: Manifest[a.Event]

    private val subscriptionsMap: TrieMap[a.Id, CancellationSource] = TrieMap.empty

    private lazy val aggregatesRef = fb / "aggregates" / a.name
    private lazy val aggregatesEventsRef = fb / "aggregateEvents" / a.name
    private lazy val projectionsRef = fb / "projections" / pr.name
    private def lastVersionRefOf(id: a.Id): Firebase = projectionsRef / id / "lastVersion"

    override def run(completeWith: CancellationToken): Observable[Try[AnyRef]] =
    {
        aggregatesRef
        .observeData[a.Id, AnyRef]
        .completeWith(completeWith)
        .flatMap
        {
            case Success(ds)  => ds match
            {
                case DataAdded(id: a.Id, _) => subscribe(completeWith, id)
                case DataRemoved(id: a.Id)  => removeSubscription(id)
                case _                      => Observable.just(Success("Unknown event"))
            }
            case x@Failure(_) => Observable.just(x)
        }

    }


    override def prepare(completeWith: CancellationToken): Future[Unit] = pr.prepare()

    private def subscribe(completeWith: CancellationToken, aggId: a.Id): Observable[Try[AnyRef]] =
    {
        val cancellation = new CancellationSource
        subscriptionsMap(aggId) = cancellation

        val lastVersionRef = lastVersionRefOf(aggId)

        lastVersionRef
        .value[String]
        .observe
        .flatMap(lastVersion =>
                 {
                     (aggregatesEventsRef / aggId)
                     .orderByKey()
                     .startAt(lastVersion.getOrElse("-1"))
                     .observeAddedData[String, a.Event]
                     .completeWith(completeWith)
                     .completeWith(cancellation)
                     .flatMapF
                     {
                         case Success(added) =>
                             pr
                             .consume(aggId, added.value)
                             .recoverAsTry
                             .flatMap(r =>
                                          //Todo: Fix this. Probably won't work very well due to race conditions
                                          (lastVersionRef <-- added.id).map(_ => r)
                                 )
                         case x              => Future.successful(x)
                     }
                 }
            )
    }

    private def removeSubscription(id: a.Id): Observable[Try[AnyRef]] =
    {
        subscriptionsMap(id).cancel()
        subscriptionsMap.remove(id)

        Observable.just(Success("Removed subscription"))
    }

}
