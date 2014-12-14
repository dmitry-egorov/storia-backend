package com.pointswarm.minions.reportStreacher

import com.firebase.client.Firebase
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.migration.Migrator
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.fireLegion.messenger.SuccessResponse
import org.json4s.Formats

import scala.concurrent._

class ReportStretcher(fb: Firebase, elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Minion[ReportCommand]
{
    def execute(commandId: CommandId, command: ReportCommand): Future[AnyRef] =
    {
        val content = command.content
        val eventId = command.eventId
        val userId = command.authorId

        val docId = eventId + "_" + userId
        val textEntry = TextIndexEntryView(eventId, content)

        elastic index "texts" doc("text", docId, textEntry) map (_ => SuccessResponse)
    }

    override def prepare: Future[Unit] = Migrator.createTextIndex(elastic)
}
