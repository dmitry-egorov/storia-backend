package com.pointswarm.projections.event

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import com.dmitryegorov.tools.extensions.SanitizeExtensions._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.HtmlContent
import com.pointswarm.domain.common.{EventIdAgg, ReportIdAgg}
import com.pointswarm.domain.reporting.Report
import com.pointswarm.domain.reporting.Report._
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent._

class EventViewReportsBuilder(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Projection[Report.type]
{
    private lazy val eventsRef: Firebase = fb / "events"
    private def eventRefOf(eventId: EventIdAgg): Firebase = eventsRef / eventId.value.sanitize
    private def reportRefOf(reportId: ReportIdAgg): Firebase = eventRefOf(reportId.eventId) / "reports" / reportId
                                                                                                          .authorId

    def project(reportId: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        val content = event match
        {
            case Added(c)  => c
            case Edited(c) => c
        }

        (reportRefOf(reportId) / "contnet" <-- content).map(_ => s"Event view '${reportId.eventId}': content of report of '${reportId.authorId}' updated.")
    }
}


