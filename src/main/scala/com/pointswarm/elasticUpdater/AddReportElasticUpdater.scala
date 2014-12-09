package com.pointswarm.elasticUpdater

import com.firebase.client.Firebase
import com.pointswarm.common._
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.processing.FirebaseCommandProcessor
import org.json4s.Formats
import rx.lang.scala.Subscription

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AddReportElasticUpdater
{
    implicit val formats = CommonFormats.formats

    def run(fb: Firebase, elastic: Client): Subscription =
    {
        val updater = new AddReportElasticUpdater(fb, elastic)
        FirebaseCommandProcessor.run(fb.child("commands").child("addReport"), "addReport", updater.addElasticText)
    }
}

class AddReportElasticUpdater(fb: Firebase, elastic: Client)(implicit f: Formats)
{
    def addElasticText(command: AddReportCommand): Future[SuccessResponse] =
    {
        val content = command.content
        val eventId = command.eventId
        val userId = command.profileId

        val docId = eventId.value + "_" + userId.value
        val textEntry = new TextIndexEntry(eventId, content)

        elastic
        .index("texts", "text")
        .doc(docId, textEntry)
        .map(_ => new SuccessResponse)
    }
}
