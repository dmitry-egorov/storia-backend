package com.pointswarm.minions.addReport

import com.firebase.client.Firebase
import com.pointswarm.common._
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.processing.Minion
import org.json4s.Formats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ReportStretcher(fb: Firebase, elastic: Client)(implicit f: Formats) extends Minion[ReportCommand]
{
    def obey(command: ReportCommand): Future[SuccessResponse] =
    {
        val content = command.content
        val eventId = command.eventId
        val userId = command.authorId

        val docId = eventId.value + "_" + userId.value
        val textEntry = new TextIndexEntry(eventId, content)

        elastic
        .index("texts", "text")
        .doc(docId, textEntry)
        .map(_ => new SuccessResponse)
    }
}
