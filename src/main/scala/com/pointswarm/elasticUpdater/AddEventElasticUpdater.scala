package com.pointswarm.elasticUpdater

import com.firebase.client.Firebase
import com.pointswarm.common.{CommonFormats, EventId, SuccessResponse, TextIndexEntry}
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.extensions.SanitizeExtensions.StringSanitizer
import com.pointswarm.tools.processing.FirebaseCommandProcessor
import org.json4s.Formats
import rx.lang.scala.Subscription

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AddEventElasticUpdater
{
    implicit val formats = CommonFormats.formats

    def run(fb: Firebase, elastic: Client): Subscription =
    {
        val updater = new AddEventElasticUpdater(fb, elastic)
        FirebaseCommandProcessor.run(fb.child("commands").child("addEvent"), "addEvent", updater.addElasticText)
    }
}

class AddEventElasticUpdater(fb: Firebase, elastic: Client)(implicit f: Formats)
{
    def addElasticText(command: AddEventCommand): Future[SuccessResponse] =
    {
        val title = command.title
        val id = new EventId(title.sanitize)
        val textEntry = new TextIndexEntry(id, title)

        elastic
        .index("texts", "text")
        .doc(id.value, textEntry)
        .map(_ => new SuccessResponse)
    }
}







