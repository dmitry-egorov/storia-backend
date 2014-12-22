package com.pointswarm.minions.aggreagtor

import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationSource
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.{EventId, HtmlContent, ProfileId}
import com.pointswarm.common.format.CommonFormats
import com.pointswarm.domain.reporting.Report._
import com.pointswarm.domain.reporting.{Report, ReportId}
import com.pointswarm.fireLegion.test.MinionTest
import com.scalasourcing.backend.firebase._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.scalatest.{FunSuite, Matchers}

class AggregatorSuite extends FunSuite with Matchers with ScalaFutures with MinionTest {
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    implicit val defaultPatience = PatienceConfig(Span(10, Seconds), Span(100, Millis))
    implicit val formats = CommonFormats.formats

    val fb = new Firebase("https://storia-test.firebaseio.com/aggregator")

    fb <-- null

    test("Should execute command")
    {
        val executor = FirebaseExecutorsBuilder(fb).and(Report).build
        val source = new CancellationSource()

        val id = ReportId(ProfileId("user1"), EventId("event1"))
        val content = HtmlContent("content")
        val payload = DoReport(content)

        val expected = Seq(Added(content))

        val run = executor.run(source).doOnNext(x => println(x)).await

        val reportRef = fb / "commands" / "report"
        val commandId = "commandId1"

        val f =
            for
            {
                _ <- reportRef / "inbox" / commandId <-- ExecuteCommand(id, payload)
                result <- (reportRef / "results" / commandId).awaitValue[Seq[Added]]()
                events <- (fb / "aggregates" / "report" / id.value / "events")
                          .awaitValue[Seq[Added]]()
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
