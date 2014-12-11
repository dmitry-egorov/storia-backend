package com.pointswarm.tools.extensions

import java.util.concurrent.CancellationException

import com.firebase.client.Firebase.CompletionListener
import com.firebase.client._
import com.pointswarm.tools.extensions.ObjectExtensions.AnyEx
import com.pointswarm.tools.extensions.SerializationExtensions._
import org.json4s.Formats
import rx.lang.scala.{Observable, Subscription}

import scala.concurrent.{Future, Promise}


object FirebaseExtensions
{

    implicit class DataSnapshotEx(ds: DataSnapshot)
    {
        def value[T](implicit m: Manifest[T], f: Formats): T =
        {
            ds.getValue.toScala.toJson.readAs[T]
        }
    }

    implicit class FirebaseEx(ref: Firebase)
    {
        def remove(): Future[Unit] =
        {
            val p = Promise[Unit]()
            ref.removeValue(createCompletionListener(p))
            p.future
        }

        def set(value: AnyRef)(implicit f: Formats): Future[Unit] =
        {
            val p = Promise[Unit]()
            val json = value.toJson
            val obj = json.readAs[Any].toJava

            ref.setValue(obj, createCompletionListener(p))

            p.future
        }

        def value: Future[DataSnapshot] =
        {
            val p = Promise[DataSnapshot]()

            val listener = new ValueEventListener
            {
                override def onDataChange(dataSnapshot: DataSnapshot): Unit = p.success(dataSnapshot)

                override def onCancelled(firebaseError: FirebaseError): Unit = p.failure(new CancellationException())
            }

            ref.addListenerForSingleValueEvent(listener)
            
            p.future
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
                        val listener = new ChildEventListener
                        {
                            override def onChildRemoved(ds: DataSnapshot): Unit = obs.onNext(new Removed(ds))

                            override def onChildMoved(ds: DataSnapshot, s: String): Unit =
                            {}

                            override def onChildChanged(ds: DataSnapshot, s: String): Unit = obs.onNext(new Changed(ds))

                            override def onCancelled(firebaseError: FirebaseError): Unit =
                            {
                                if (firebaseError == null)
                                {
                                    obs.onCompleted()
                                }
                                else
                                {
                                    obs.onError(firebaseError.toException)
                                }
                            }

                            override def onChildAdded(ds: DataSnapshot, s: String): Unit = obs.onNext(new Added(ds))
                        }

                        ref.addChildEventListener(listener)

                        Subscription
                        {
                            ref.removeEventListener(listener)
                        }
                    })
        }

        private def createCompletionListener(p: Promise[Unit]): CompletionListener =
        {
            new CompletionListener
            {
                override def onComplete(firebaseError: FirebaseError, firebase: Firebase): Unit =
                {
                    if (firebaseError == null)
                    {
                        p.success(())
                    }
                    else
                    {
                        p.failure(firebaseError.toException)
                    }
                }
            }
        }
    }


}


sealed trait Event

case class Added(ds: DataSnapshot) extends Event

case class Removed(ds: DataSnapshot) extends Event

case class Changed(ds: DataSnapshot) extends Event


