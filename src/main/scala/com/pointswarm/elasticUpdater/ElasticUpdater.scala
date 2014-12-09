package com.pointswarm.elasticUpdater

import com.firebase.client.Firebase
import com.pointswarm.common.{EventId, SuccessResponse, TextIndexEntry}
import com.pointswarm.extensions.SanitizeExtensions.StringSanitizer
import com.pointswarm.processing.FirebaseCommandProcessor
import com.pointswarm.serialization.CommonFormats
import com.pointswarm.elastic._
import rx.lang.scala.Subscription
//import wabisabi.Client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ElasticUpdater
{
    def run(fb: Firebase, elastic: Client) = new ElasticUpdater(fb, elastic).run()
}

class ElasticUpdater(fb: Firebase, elastic: Client)
{
    implicit val formats = CommonFormats.formats

    def run(): Subscription =
    {
        FirebaseCommandProcessor.run(fb, "addEvent", addElasticText)
    }

    private def addElasticText(command: AddEventCommand): Future[SuccessResponse] =
    {
        val title = command.title
        val id = new EventId(title.sanitize)
        val textEntry = new TextIndexEntry(id, title)

        elastic
        .index("texts", "text")
        .doc(textEntry)
        .map(_ => new SuccessResponse)
    }
}




