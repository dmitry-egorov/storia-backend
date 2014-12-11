package com.pointswarm.minions.report

import com.firebase.client.Firebase
import com.pointswarm.application.migration.Migrator
import com.pointswarm.common._
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.processing.Minion
import org.json4s.Formats

import scala.concurrent._

class ReportStretcher(fb: Firebase, elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Minion[ReportCommand]
{
    def execute(command: ReportCommand): Future[SuccessResponse] =
    {
        val content = command.content
        val eventId = command.eventId
        val userId = command.authorId

        val docId = eventId.value + "_" + userId.value
        val textEntry = new TextIndexEntry(eventId, content)

        elastic
        .index("texts")
        .doc("text", docId, textEntry)
        .map(_ => new SuccessResponse)
    }

    def prepare: Future[Unit] = Migrator.createTextIndex(elastic)
}
