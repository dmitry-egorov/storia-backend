package com.pointswarm.minions.addEvent

import com.firebase.client.Firebase
import com.pointswarm.common._
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.extensions.SanitizeExtensions._
import com.pointswarm.tools.processing._
import org.json4s.Formats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EventStretcher(fb: Firebase, elastic: Client)(implicit f: Formats) extends Minion[AddEventCommand]
{
    def obey(command: AddEventCommand): Future[SuccessResponse] =
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









