package com.pointswarm.minions.eventStretcher

import com.dmitryegorov.tools.elastic._
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.fireLegion._
import com.pointswarm.fireLegion.messenger.SuccessResponse
import com.pointswarm.migration.Migrator
import org.json4s.Formats

import scala.concurrent._

class EventStretcher(elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Minion[AddEventCommand]
{
    def execute(commandId: CommandId, command: AddEventCommand): Future[AnyRef] =
    {
        val title = command.title
        val id = EventId(title.sanitize)
        val textEntry = TextIndexEntryView(id, title)

        elastic index "texts" doc("text", id, textEntry) map (_ => SuccessResponse)
    }

    override def prepare: Future[Unit] = Migrator.createTextIndex(elastic)
}









