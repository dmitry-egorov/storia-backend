package com.dmitryegorov.hellfire

import java.util.concurrent.CancellationException

import com.dmitryegorov.hellfire.Tools._
import com.firebase.client.Firebase.CompletionListener
import com.firebase.client.Transaction.{Handler, Result}
import com.firebase.client._
import org.json4s.Formats
import rx.lang.scala._

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util.Try

object Hellfire
{
    implicit class RichDataSnapshot(val ds: DataSnapshot) extends AnyVal
    {
        def value[T: Manifest](implicit f: Formats): Option[T] =
        {
            extract(ds.getValue)
        }
    }

    implicit class RichQuery(val ref: Query) extends AnyVal
    {
        def current: Future[DataSnapshot] =
        {
            val p = Promise[DataSnapshot]()

            val listener = createSingleValueEventListener(p)

            ref.addListenerForSingleValueEvent(listener)

            p.future
        }

        def value[T: Manifest](implicit f: Formats, ec: ExecutionContext): Future[Option[T]] =
        {
            current.map(x => x.value[T])
        }

        def whenExists(f: () => Unit)(implicit ec: ExecutionContext): Future[Unit] = exists
                                                                                     .map(exists => if (exists) f())

        def exists(implicit ec: ExecutionContext): Future[Boolean] =
        {
            current.map(x => x.exists())
        }

        def await(timeout: Duration = Duration.Inf)(implicit ec: ExecutionContext): Future[DataSnapshot] =
        {
            val p = Promise[DataSnapshot]()

            val listener = createWatchValueEventListener(p)

            ref.addValueEventListener(listener)

            p.future.timeout(timeout).andThen
            {
                case _ => ref.removeEventListener(listener)
            }
        }

        def awaitValue[T: Manifest](timeout: Duration = Duration
                                                        .Inf)(implicit f: Formats, ec: ExecutionContext): Future[T] =
        {
            await(timeout).map(x => x.value[T].get)
        }

        def observeAddedValues[T: Manifest](implicit f: Formats, ec: ExecutionContext): Observable[T] =
        {
            observeAdded.map(x => x.value[T].get)
        }

        def observeAdded: Observable[DataSnapshot] =
        {
            observe
            .collect
            {
                case SnapAdded(ds) => ds
            }
        }

        def observeAddedData[V: Manifest](implicit f: Formats): Observable[Try[DataAdded[V]]] =
        {
            observeData[V]
            .collect
            {
                case x: Try[DataAdded[V]] => x
            }
        }

        def observe: Observable[SnapEvent] =
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

        def observeData[V: Manifest](implicit f: Formats): Observable[Try[DataEvent]] =
        {
            observe.map
            {
                case SnapAdded(ds)   => Try(DataAdded[V](ds.getKey, ds.value[V].get))
                case SnapChanged(ds) => Try(DataChanged[V](ds.getKey, ds.value[V].get))
                case SnapRemoved(ds) => Try(DataRemoved(ds.getKey))
            }
        }
    }

    implicit class RichFirebase(val ref: Firebase) extends AnyVal
    {
        def /(path: String) = ref.child(path)

        def newKey: String = ref.push().getKey

        def remove: Future[String] =
        {
            val p = Promise[String]()
            ref.removeValue(createCompletionListener(p))
            p.future
        }

        def <--(value: Boolean)(implicit f: Formats): Future[String] = set(value)
        def <--(value: String)(implicit f: Formats): Future[String] = set(value)
        def <--(value: Any)(implicit f: Formats): Future[String] = set(value)
        def <#-(value: Product)(implicit f: Formats): Future[String] = update(value)
        def <%-(value: Boolean)(implicit f: Formats): Future[String] = push(value)
        def <%-(value: String)(implicit f: Formats): Future[String] = push(value)
        def <%-(value: AnyRef)(implicit f: Formats): Future[String] = push(value)

        def push(value: Boolean)(implicit f: Formats): Future[String] = ref.push.set(value)
        def push(value: String)(implicit f: Formats): Future[String] = ref.push.set(value)
        def push(value: AnyRef)(implicit f: Formats): Future[String] = ref.push.set(value)

        def set(value: Boolean)(implicit f: Formats): Future[String] = set(value: java.lang.Boolean)
        def set(value: String)(implicit f: Formats): Future[String] = set(value: AnyRef)

        def set(value: Any)(implicit f: Formats): Future[String] =
        {
            val obj = value.toJValue.toJava

            val p = Promise[String]()

            ref.setValue(obj, createCompletionListener(p))

            p.future
        }

        def update(value: Product)(implicit f: Formats): Future[String] =
        {
            val obj = value.toJValue.toJava.asInstanceOf[java.util.Map[String, AnyRef]]

            val p = Promise[String]()

            ref.updateChildren(obj, createCompletionListener(p))

            p.future
        }

        def transaction[T: Manifest](f: Option[T] => Option[T])(implicit fmt: Formats): Future[TransactionResult[T]] =
        {
            val p = Promise[TransactionResult[T]]()

            ref.runTransaction(createTransactionHandler(f, p))

            p.future
        }
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

    private def createChildEventListener(observer: Observer[SnapEvent]): ChildEventListener =
    {
        new ChildEventListener
        {
            override def onChildRemoved(ds: DataSnapshot): Unit = observer.onNext(new SnapRemoved(ds))

            override def onChildMoved(ds: DataSnapshot, s: String): Unit =
            {}

            override def onChildChanged(ds: DataSnapshot, s: String): Unit = observer.onNext(new SnapChanged(ds))

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

            override def onChildAdded(ds: DataSnapshot, s: String): Unit = observer.onNext(new SnapAdded(ds))
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

    private def createTransactionHandler[T: Manifest](f: (Option[T]) => Option[T], p: Promise[TransactionResult[T]])(implicit fmt: Formats): Handler =
    {
        new Handler
        {
            override def doTransaction(mutableData: MutableData): Result =
            {
                val result = f(extract[T](mutableData.getValue))
                if (result.isDefined)
                {
                    mutableData.setValue(result.get.toJValue.toJava)
                    Transaction.success(mutableData)
                }
                else
                {
                    Transaction.abort()
                }
            }

            override def onComplete(firebaseError: FirebaseError, committed: Boolean, dataSnapshot: DataSnapshot): Unit =
            {
                if (firebaseError == null)
                {
                    p.success(new TransactionResult[T](committed, extract(dataSnapshot.getValue)))
                }
                else
                {
                    p.failure(firebaseError.toException)
                }
            }
        }
    }

    private def extract[T: Manifest](value: AnyRef)(implicit fmt: Formats): Option[T] =
    {
        Option(value).map(x => x.fromJavaToJValue.extract[T])
    }

    case class TransactionResult[T](committed: Boolean, finalData: Option[T])
}
