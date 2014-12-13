package com.pointswarm.minions.eventStretcher

import com.firebase.client.Firebase
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.migration.Migrator
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.extensions.SanitizeExtensions._
import com.pointswarm.tools.fireLegion._
import org.json4s.Formats

import scala.concurrent._

class EventStretcher(fb: Firebase, elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Minion[AddEventCommand]
{
    def execute(commandId: CommandId, command: AddEventCommand): Future[AnyRef] =
    {
        val title = command.title
        val id = new EventId(title.sanitize)
        val textEntry = new TextIndexEntryView(id, title)

        elastic
        .index("texts")
        .doc("text", id.value, textEntry)
        .map(_ => SuccessResponse)
    }

    override def prepare: Future[Unit] = Migrator.createTextIndex(elastic)
}









