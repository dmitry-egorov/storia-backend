package com.scalasourcing.backend.firebase

import com.firebase.client.Firebase
import com.dmitryegorov.hellfire.Hellfire._
import com.scalasourcing.backend.{Tester, EventStorageSuite}

class FirebaseEventStorageSuite extends EventStorageSuite
{
    implicit val formats = CommonFormats.formats

    private val fb = new Firebase("https://scalasourcing.firebaseio.com/es")

    fb <-- null

    var testIndex = 0

    override def createStorage =
    {
        testIndex += 1

        FirebaseEventStorage(Tester)(fb / ("test" + testIndex))
    }
}




