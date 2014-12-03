package com.pointswarm.extensions

import com.firebase.client.{ChildEventListener, DataSnapshot, Firebase, FirebaseError}

object FirebaseExtensions
{
    implicit class FirebaseEx(ref: Firebase)
    {
        def onValueAdded(f: (DataSnapshot, String) => Unit) =
        {
            ref.addChildEventListener(new ChildEventListener
            {
                override def onChildRemoved(dataSnapshot: DataSnapshot): Unit = {}

                override def onChildMoved(dataSnapshot: DataSnapshot, s: String): Unit = {}

                override def onChildChanged(dataSnapshot: DataSnapshot, s: String): Unit = {}

                override def onCancelled(firebaseError: FirebaseError): Unit = {}

                override def onChildAdded(dataSnapshot: DataSnapshot, s: String): Unit = f(dataSnapshot, s)
            })
        }
    }
}