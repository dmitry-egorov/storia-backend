package com.scalasourcing.backend.firebase

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.firebase.domain.CommonFormats
import com.scalasourcing.backend.{EventStorageSuite, Tester}

class FirebaseEventStorageSuite extends EventStorageSuite
{
    implicit val formats = CommonFormats.formats

    private val fb = new Firebase("https://scalasourcing.firebaseio.com/es")

    fb <-- null

    var testIndex = 0

    override def createStorage =
    {
        testIndex += 1

        new FirebaseEventStorage(Tester)(fb / ("test" + testIndex))
    }
}




