package com.scalasourcing.backend

import com.dmitryegorov.futuristic.FutureExtensions._
import com.scalasourcing.backend.Tester.{DoSomething, SomethingHappened}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}

abstract class EventStorageSuite extends FunSuite with Matchers with ScalaFutures {
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val defaultPatience = PatienceConfig(Span(5, Seconds), Span(100, Millis))

    val id1 = TesterId("1")
    val id2 = TesterId("2")

    test("Should return empty messages when nothing was added")
    {
        //given
        val es = createStorage

        //when
        val f = es.get(id1)

        //then
        whenReady(f)
        {
            events => events should be(empty)
        }
    }

    test("Should return persisted messages")
    {
        //given
        val es = createStorage

        val persistedEvents = Seq(SomethingHappened())

        val f =
            es.tryPersist(id1, persistedEvents, 0)
            //when
            .flatMap(_ => es.get(id1))

        //then
        whenReady(f)
        {
            events => events should equal(persistedEvents)
        }
    }

    test("Should return persisted messages for each aggregate instance")
    {
        //given
        val es = createStorage

        val persistedEvents1 = Seq(SomethingHappened())
        val persistedEvents2 = Seq(SomethingHappened(), SomethingHappened())

        val f =
            for
            {
                _ <- es
                     .tryPersist(id1, persistedEvents1, 0)
                _ <- es
                     .tryPersist(id2, persistedEvents2, 0)

                //when
                events1 <- es.get(id1)
                events2 <- es.get(id2)
            }
            yield (events1, events2)


        //then
        whenReady(f)
        {
            (e) =>
                e
                ._1 should equal(persistedEvents1)
                e
                ._2 should equal(persistedEvents2)
        }
    }

    if (testMultiThreading)
    {
        test("Should save all messages from multiple threads")
        {
            val es = createStorage

            val times = 5
            val f =
                List
                .fill(times)(DoSomething())
                .map(c => es.execute(id1, c))
                .waitAll
                .flatMap(_ => es.get(id1))

            whenReady(f)
            {
                events => events
                          .length should equal(times)
            }
        }
    }

    def createStorage: EventStorage
        {val a: Tester.type}

    def testMultiThreading = true
}
