package com.scalasourcing.backend.firebase

import com.firebase.client.Firebase
import com.dmitryegorov.hellfire.Hellfire._
import com.scalasourcing.backend.Root
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

class FirebaseAggregatorSuite extends FunSuite with ScalaFutures
{
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val defaultPatience = PatienceConfig(Span(5, Seconds), Span(100, Millis))
    implicit val formats = CommonFormats.formats

    val fb = new Firebase("https://storia-test.firebaseio.com/aggregator")

    fb <-- null

    test("Should listen and execute commands")
    {
        val agg = new FirebaseAggregator[Root]

//        agg.run()
    }
}
