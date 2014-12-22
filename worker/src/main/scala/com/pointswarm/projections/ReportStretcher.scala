package com.pointswarm.projections

import com.dmitryegorov.tools.elastic.Client
import com.pointswarm.common.views.TextIndexEntryView
import com.pointswarm.domain.reporting.{ReportId, Report}
import com.pointswarm.domain.reporting.Report.{Added, Edited}
import com.pointswarm.migration.Migrator
import com.scalasourcing.model.AggregateEvent
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class ReportStretcher(elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Projection[ReportId]
{
    def consume(id: Report.Id, event: AggregateEvent): Future[Unit] =
    {
        val content = event match
        {
            case Added(c)  => c
            case Edited(c) => c
        }

        val docId = id.eventId + "_" + id.userId
        val textEntry = TextIndexEntryView(id.eventId, content)

        elastic index "texts" doc("text", docId, textEntry) map (_ => ())
    }

    def prepare: Future[Unit] = Migrator.createTextIndex(elastic)
}
