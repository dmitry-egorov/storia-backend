package com.pointswarm.minions.aggreagtor

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.commands.DoAggregateCommand
import com.pointswarm.common.dtos.{EventId, HtmlContent, ProfileId}
import com.pointswarm.common.format.CommonFormats
import com.pointswarm.domain.reporting.Report
import com.pointswarm.domain.reporting.Report._
import com.pointswarm.fireLegion.test.MinionTest
import com.pointswarm.minions.aggregator.Aggregator
import com.scalasourcing.backend.firebase.FirebaseEventStorage
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.scalatest.{FunSuite, Matchers}

class AggregatorSuite extends FunSuite with Matchers with ScalaFutures with MinionTest
{
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val defaultPatience = PatienceConfig(Span(5, Seconds), Span(100, Millis))
    implicit val formats = CommonFormats.formats

    val fb = new Firebase("https://storia-test.firebaseio.com/aggregator")

    fb <-- null

    test("Should execute command")
    {
        val es = new FirebaseEventStorage(fb)
        val aggregator = new Aggregator(es)
        aggregator.register[Report]()

        val id = Id(ProfileId("user1"), EventId("event1"))
        val content = HtmlContent("content")
        val payload = DoReport(content)
        val command = DoAggregateCommand(id, payload)

        val expected = Seq(Created(content))

        val f = execute[DoAggregateCommand, Seq[Created]](fb, aggregator, command)

        whenReady(f)
        {
            events =>
            {
                events should equal(expected)
                whenReady((fb / "aggregates" / "report" / id.value / "events").awaitValue[Seq[Created]]())
                {
                    events => events should equal(expected)
                }
            }
        }
    }
}
