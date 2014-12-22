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


class FirebaseExecutorSuite extends FunSuite with Matchers with ScalaFutures {
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val defaultPatience = PatienceConfig(Span(5, Seconds), Span(100, Millis))
    implicit val formats = CommonFormats.formats

    val fb = new Firebase("https://storia-test.firebaseio.com/ag")

    fb <-- null

    val source = new CancellationSource()

    test("Should listen and execute commands") {
                                                   val fe = FirebaseExecutorsBuilder(fb).and(Tester).build

                                                   val run = fe.run(source).doOnNext(x => println(x)).await

                                                   val rootRef = fb / "commands" / "tester"
                                                   val commandId = "commandId1"
                                                   val rootId = TesterId("rootId1")
                                                   val expected = Seq(SomethingHappened())

                                                   val f =
                                                       for {
                                                           _ <- rootRef / "inbox" / commandId <-- ExecuteCommand(rootId, DoSomething())
                                                           result <- (rootRef / "results" / commandId)
                                                                     .awaitValue[Seq[SomethingHappened]]()
                                                           events <- (fb / "aggregates" / "tester" / rootId / "events")
                                                                     .awaitValue[Seq[SomethingHappened]]()
                                                       }
                                                       yield (result, events)

                                                   whenReady(f) {
                                                                    t => {
                                                                        t._1 should equal(expected)
                                                                        t._2 should equal(expected)
                                                                        source.cancel()
                                                                        whenReady(run) { _ => () }
                                                                    }
                                                                }
                                               }
}
