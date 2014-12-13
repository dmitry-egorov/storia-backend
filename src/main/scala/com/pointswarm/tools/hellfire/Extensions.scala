package com.pointswarm.tools.hellfire

import java.util.concurrent.CancellationException

import com.firebase.client.Firebase.CompletionListener
import com.firebase.client._
import com.pointswarm.tools.extensions.ObjectExtensions._
import com.pointswarm.tools.extensions.SerializationExtensions._
import org.json4s.Formats
import rx.lang.scala._

import scala.concurrent._

object Extensions
{

    case class FireResult[TValue](key: String, value: TValue)

    implicit class DataSnapshotEx(val ds: DataSnapshot) extends AnyVal
    {
        def result[T](implicit m: Manifest[T], f: Formats): Option[FireResult[T]] =
        {
            value[T].map(v => new FireResult(ds.getKey, v))
        }

        def value[T](implicit m: Manifest[T], f: Formats): Option[T] =
        {
            Option(ds.getValue).map(x => x.toScala.toJson.readAs[T])
        }
    }

    implicit class FirebaseEx(val ref: Firebase) extends AnyVal
    {
        def newKey: String = ref.push().getKey

        def remove(): Future[String] =
        {
            val p = Promise[String]()
            ref.removeValue(createCompletionListener(p))
            p.future
        }

        def push(value: AnyRef)(implicit f: Formats): Future[String] = ref.push.set(value)

        def set(value: AnyRef)(implicit f: Formats): Future[String] =
        {
            val p = Promise[String]()
            val obj = value.toJson.readAs[Any].toJava

            ref.setValue(obj, createCompletionListener(p))

            p.future
        }

        def current: Future[DataSnapshot] =
        {
            val p = Promise[DataSnapshot]()

            val listener = createSingleValueEventListener(p)

            ref.addListenerForSingleValueEvent(listener)

            p.future
        }

        def result[T](implicit m: Manifest[T], f: Formats, ec: ExecutionContext): Future[Option[FireResult[T]]] =
        {
            current.map(x => x.result[T])
        }

        def value[T](implicit m: Manifest[T], f: Formats, ec: ExecutionContext): Future[Option[T]] =
        {
            current.map(x => x.value[T])
        }

        def exists(implicit ec: ExecutionContext): Future[Boolean] =
        {
            current.map(x => x.exists())
        }

        def await(implicit ec: ExecutionContext): Future[DataSnapshot] =
        {
            val p = Promise[DataSnapshot]()

            val listener = createWatchValueEventListener(p)

            ref.addValueEventListener(listener)

            p.future.andThen
            { case _ => ref.removeEventListener(listener) }
        }

        def awaitResult[T](implicit m: Manifest[T], f: Formats, ec: ExecutionContext): Future[FireResult[T]] =
        {
            await.map(x => x.result[T].get)
        }

        def awaitValue[T](implicit m: Manifest[T], f: Formats, ec: ExecutionContext): Future[T] =
        {
            await.map(x => x.value[T].get)
        }

        def observeAdded: Observable[Added] =
        {
            observe
            .collect
            {
                case x: Added => x
            }
        }

        def observe: Observable[Event] =
        {
            Observable
            .create(obs =>
                    {
                        val listener = createChildEventListener(obs)

                        ref.addChildEventListener(listener)

                        Subscription
                        {
                            ref.removeEventListener(listener)
                        }
                    })
        }

        private def createWatchValueEventListener(promise: Promise[DataSnapshot]): ValueEventListener with Object =
        {
            new ValueEventListener
            {
                override def onDataChange(ds: DataSnapshot): Unit = if (ds.exists) promise.success(ds)

                override def onCancelled(firebaseError: FirebaseError): Unit = promise
                                                                               .failure(new CancellationException())
            }
        }

        private def createSingleValueEventListener(promise: Promise[DataSnapshot]): ValueEventListener with Object =
        {
            new ValueEventListener
            {
                override def onDataChange(dataSnapshot: DataSnapshot): Unit = promise.success(dataSnapshot)

                override def onCancelled(firebaseError: FirebaseError): Unit = promise
                                                                               .failure(new CancellationException())
            }
        }

        private def createChildEventListener(observer: Observer[Event]): ChildEventListener =
        {
            new ChildEventListener
            {
                override def onChildRemoved(ds: DataSnapshot): Unit = observer.onNext(new Removed(ds))

                override def onChildMoved(ds: DataSnapshot, s: String): Unit =
                {}

                override def onChildChanged(ds: DataSnapshot, s: String): Unit = observer.onNext(new Changed(ds))

                override def onCancelled(firebaseError: FirebaseError): Unit =
                {
                    if (firebaseError == null)
                    {
                        observer.onCompleted()
                    }
                    else
                    {
                        observer.onError(firebaseError.toException)
                    }
                }

                override def onChildAdded(ds: DataSnapshot, s: String): Unit = observer.onNext(new Added(ds))
            }
        }

        private def createCompletionListener(promise: Promise[String]): CompletionListener =
        {
            new CompletionListener
            {
                override def onComplete(firebaseError: FirebaseError, firebase: Firebase): Unit =
                {
                    if (firebaseError == null)
                    {
                        promise.success(firebase.getKey)
                    }
                    else
                    {
                        promise.failure(firebaseError.toException)
                    }
                }
            }
        }
    }

}
