package com.scalasourcing.backend.firebase

import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationSource
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.TestRoot.{RootCommand, RootEvent}
import com.scalasourcing.backend._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}

class FirebaseExecutorSuite extends FunSuite with Matchers with ScalaFutures
{
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val defaultPatience = PatienceConfig(Span(5, Seconds), Span(100, Millis))
    implicit val formats = CommonFormats.formats

    val fb = new Firebase("https://storia-test.firebaseio.com/ag")
    val es = new FirebaseEventStorage(fb)

    fb <-- null

    val source = new CancellationSource()

    test("Should listen and execute commands")
    {
        val agg = FirebaseExecutor(TestRoot)(fb, es)

        val run = agg.run(source).doOnNext(x => println(x)).await

        val rootRef = fb / "commands" / "testRoot"
        val commandId = "commandId1"
        val rootId = TestRootId("rootId1")
        val expected = Seq(RootEvent())

        val f =
            for
            {
                _ <- rootRef / "inbox" / commandId <-- ExecuteCommand(rootId, RootCommand())
                result <- (rootRef / "results" / commandId).awaitValue[Seq[RootEvent]]()
                events <- (fb / "aggregates" / "testRoot" / rootId / "events").awaitValue[Seq[RootEvent]]()
            }
            yield (result, events)

        whenReady(f)
        {
            t =>
            {
                t._1 should equal(expected)
                t._2 should equal(expected)
                source.cancel()
                whenReady(run)
                { _ => () }
            }
        }
    }
}
