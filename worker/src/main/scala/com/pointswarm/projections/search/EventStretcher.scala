package com.pointswarm.projections.search

import com.dmitryegorov.tools.elastic.Client
import com.pointswarm.common.views.TextIndexEntryView
import com.pointswarm.domain.reporting.Event
import com.pointswarm.domain.reporting.Event._
import com.pointswarm.migration.Migrator
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class EventStretcher(elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Projection[Event.type]
{
    def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        val title = event match
        {
            case Created(t) => t
        }

        val hash = id.hash

        val textEntry = TextIndexEntryView(hash, title)

        elastic index "texts" doc("text", id, textEntry) map (_ => s"Stretched event '$id'")
    }

    override def prepare(): Future[Unit] = Migrator.createTextIndex(elastic)
}
