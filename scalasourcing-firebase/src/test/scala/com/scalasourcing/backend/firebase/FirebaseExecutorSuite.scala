package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationSource
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.Tester.{DoSomething, SomethingHappened}
import com.scalasourcing.backend.{Tester, TesterId}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}


class FirebaseExecutorSuite extends FunSuite with Matchers with ScalaFutures
{
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val defaultPatience = PatienceConfig(Span(10, Seconds), Span(300, Millis))
    implicit val formats = CommonFormats.formats

    val fb = new Firebase("https://storia-test.firebaseio.com/ag")

    fb <-- null

    val source = new CancellationSource()

    test("Should execute commands and run projections")
    {
        val fe = FirebaseExecutorsBuilder(fb)
                 .aggregate(Tester)
                 .projection(Tester)(new TesterProjection(fb))
                 .build

        val run = fe.run(source).doOnNext(x => println(x)).await

        val rootRef = fb / "commands" / "tester"
        val commandId = "commandId1"
        val rootId = TesterId("rootId1")
        val expected = Seq(SomethingHappened)

        val f =
            for
            {
                _ <- rootRef / "inbox" / commandId <-- AggregateCommand(rootId, DoSomething)
                result <- (rootRef / "results" / commandId).awaitValue[Seq[SomethingHappened.type]]()
                events <- (fb / "aggregateEvents" / "tester" / rootId ).awaitValue[Seq[SomethingHappened.type]]()
                view <- (fb / "views" / "tester" / rootId).awaitValue[Boolean]()
            }
            yield (result, events, view)

        whenReady(f)
        {
            t =>
            {
                //Todo: case objects are not equal after deserialization!
                t._1(0).getClass should equal (expected(0).getClass)
                t._2(0).getClass should equal (expected(0).getClass)
                t._3 should equal (true)
                source.cancel()
                whenReady(run)
                { _ => () }
            }
        }
    }
}
