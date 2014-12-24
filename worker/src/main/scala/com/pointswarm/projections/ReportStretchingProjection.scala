package com.pointswarm.projections

import com.dmitryegorov.tools.elastic.Client
import com.pointswarm.common.dtos.EventId
import com.pointswarm.common.views.TextIndexEntryView
import com.pointswarm.domain.reporting.Report
import com.pointswarm.domain.reporting.Report.{Added, Edited}
import com.pointswarm.migration.Migrator
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class ReportStretchingProjection(elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Projection(Report)
{
    def consume(id: a.Id, event: a.Event): Future[AnyRef] =
    {
        val content = event match
        {
            case Added(c)  => c
            case Edited(c) => c
        }

        val docId = id.eventId + "_" + id.authorId
        val textEntry = TextIndexEntryView(new EventId(id.eventId.value), content)

        elastic index "texts" doc("text", docId, textEntry) map(_ => docId)
    }

    def prepare(): Future[Unit] = Migrator.createTextIndex(elastic)
}
