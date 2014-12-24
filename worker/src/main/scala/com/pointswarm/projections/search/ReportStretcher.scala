package com.pointswarm.projections.search

import com.dmitryegorov.tools.elastic.Client
import com.pointswarm.common.dtos.EventId
import com.pointswarm.common.views.TextIndexEntryView
import com.pointswarm.domain.reporting.Report
import com.pointswarm.domain.reporting.Report.{Added, Edited}
import com.pointswarm.migration.Migrator
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class ReportStretcher(elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Projection[Report.type]
{
    def project(id: Report.Id, event: Report.Event, eventIndex: Int): Future[AnyRef] =
    {
        val content = event match
        {
            case Added(c)  => c
            case Edited(c) => c
        }

        val textEntry = TextIndexEntryView(EventId(id.eventId.value), content)

        elastic index "texts" doc("text", id.hash, textEntry) map (_ => s"Stretched report '$id'")
    }

    override def prepare(): Future[Unit] = Migrator.createTextIndex(elastic)
}
