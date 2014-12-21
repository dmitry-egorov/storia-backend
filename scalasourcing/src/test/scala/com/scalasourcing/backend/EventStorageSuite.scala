package com.scalasourcing.backend

import com.scalasourcing.backend.firebase.Tools._
import com.scalasourcing.backend.Root.{RootCommand, RootEvent, Id}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{FunSuite, Matchers}

abstract class EventStorageSuite extends FunSuite with Matchers with ScalaFutures
{
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val defaultPatience = PatienceConfig(Span(5, Seconds), Span(100, Millis))

    val id = Id("root0")
    val id1 = Id("root1")
    val id2 = Id("root2")

    test("Should return empty events when nothing was added")
    {
        //given
        val es = createStorage

        //when
        val f = es.get(id)

        //then
        whenReady(f)
        {
            _ should be(empty)
        }
    }

    test("Should return persisted events")
    {
        //given
        val es = createStorage

        val persistedEvents = Seq(RootEvent())

        val f = for
        {
            _ <- es.tryPersist(id, persistedEvents, 0)

            events <- es.get(id)
        } yield events

        whenReady(f)
        {
            _ should equal(persistedEvents)
        }
    }

    test("Should save events twice")
    {
        //given
        val es = createStorage

        val events = Seq(RootEvent())

        val f = for
        {
            _ <- es.tryPersist(id, events, 0)
            _ <- es.persist(id, events, events.length)

            events <- es.get(id)
        } yield events

        whenReady(f)
        {
            _ should equal(events ++ events)
        }
    }

    test("Should return persisted events for each aggregate instance")
    {
        //given
        val es = createStorage

        val persistedEvents1 = Seq(RootEvent())
        val persistedEvents2 = Seq(RootEvent(), RootEvent())

        val f = for
        {
            _ <- es.tryPersist(id1, persistedEvents1, 0)
            _ <- es.tryPersist(id2, persistedEvents2, 0)

            //when
            events1 <- es.get(id1)
            events2 <- es.get(id2)
        } yield (events1, events2)

        //then
        whenReady(f)
        { (e) =>
            e._1 should equal(persistedEvents1)
            e._2 should equal(persistedEvents2)
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
                .fill(times)(RootCommand())
                .map(c => es.execute(id, c))
                .waitAll
                .flatMap(_ => es.get(id))

            whenReady(f)
            {
                events => events.length should equal(times)
            }
        }
    }

    def testMultiThreading = true
    def createStorage: EventStorage
}
