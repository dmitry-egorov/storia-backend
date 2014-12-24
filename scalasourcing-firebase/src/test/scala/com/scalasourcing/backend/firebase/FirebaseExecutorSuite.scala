package com.scalasourcing.backend.firebase

import java.lang.System.err

import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationSource
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend._
import com.scalasourcing.backend.firebase.ThrowableExtensions._
import com.scalasourcing.backend.Tester._
import com.scalasourcing.backend.firebase.domain._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}

import scala.util.{Failure, Success, Try}


class FirebaseExecutorSuite extends FunSuite with Matchers with ScalaFutures
{
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val defaultPatience = PatienceConfig(Span(10000, Seconds), Span(300, Millis))
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

        val run = fe.run(source).doOnNext(print).await

        val rootRef = fb / "commands" / "tester"
        val commandId = "commandId1"
        val rootId = TesterId(SubId1("subId11"), SubId2("subId21"))
        val expected = Seq(SomethingHappened)

        val f =
            for
            {
                _ <- rootRef / "inbox" / commandId <-- AggregateCommand(rootId, DoSomething)
                result <- (rootRef / "results" / commandId / "result").awaitValue[Seq[SomethingHappened.type]]()
                events <- (fb / "aggregateEvents" / "tester" / rootId.hash).awaitValue[Seq[SomethingHappened.type]]()
                view <- (fb / "views" / "tester" / rootId.hash).awaitValue[Boolean]()
            }
            yield (result, events, view)

        whenReady(f)
        {
            t =>
            {
                //Todo: case objects are not equal after deserialization!
                t._1(0).getClass should equal(expected(0).getClass)
                t._2(0).getClass should equal(expected(0).getClass)
                t._3 should equal(true)
                source.cancel()
                whenReady(run)
                { _ => () }
            }
        }
    }

    def print(x: Try[Any]) = x match
    {
        case Success(event) => println(event)
        case Failure(error) => err.println(error.fullMessage)
    }
}
