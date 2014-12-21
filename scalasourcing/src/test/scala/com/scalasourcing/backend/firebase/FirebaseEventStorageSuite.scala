package com.scalasourcing.backend.firebase

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.EventStorageSuite
import com.scalasourcing.backend.Root.RootEvent
import org.json4s.{DefaultFormats, Formats, ShortTypeHints}

class FirebaseEventStorageSuite extends EventStorageSuite
{
    implicit val formats = new Formats
    {
        val dateFormat = DefaultFormats.lossless.dateFormat
        override val typeHints = ShortTypeHints(List(classOf[RootEvent]))
        override val typeHintFieldName = "type"
    }

    private val fb = new Firebase("https://scalasourcing.firebaseio.com/es")

    fb <-- null

    var testIndex = 0

    override def createStorage =
    {
        testIndex += 1

        new FirebaseEventStorage(fb / ("test" + testIndex))
    }
}




