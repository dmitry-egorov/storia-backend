package com.pointswarm.minions.addEvent

import com.firebase.client.Firebase
import com.pointswarm.application.migration.Migrator
import com.pointswarm.common._
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.extensions.SanitizeExtensions._
import com.pointswarm.tools.processing._
import org.json4s.Formats

import scala.concurrent._

class EventStretcher(fb: Firebase, elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Minion[AddEventCommand]
{
    def execute(command: AddEventCommand): Future[SuccessResponse] =
    {
        val title = command.title
        val id = new EventId(title.sanitize)
        val textEntry = new TextIndexEntry(id, title)

        elastic
        .index("texts")
        .doc("text", id.value, textEntry)
        .map(_ => new SuccessResponse)
    }

    def prepare: Future[Unit] = Migrator.createTextIndex(elastic)
}









