package com.pointswarm.minions.addEvent

import com.firebase.client.Firebase
import com.github.nscala_time.time.Imports._
import com.pointswarm.common._
import com.pointswarm.tools.extensions.FirebaseExtensions._
import com.pointswarm.tools.extensions.SanitizeExtensions._
import com.pointswarm.tools.processing.Minion
import org.json4s.Formats

import scala.async.Async._
import scala.concurrent._

class EventViewGenerator(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[AddEventCommand]
{
    def execute(command: AddEventCommand): Future[SuccessResponse] =
        async
        {
            val title = command.title
            val id = new EventId(title.sanitize)

            val eventRef = fb.child("events").child(id.value)

            val ds = await(eventRef.value)

            if (ds.getValue != null)
            {
                throw new EventAlreadyExistsError(id)
            }

            val view = new EventView(title, DateTime.now(DateTimeZone.UTC), None, List.empty)

            await(eventRef.set(view))

            new SuccessResponse
        }

    def prepare: Future[Unit] = async {}
}

class EventAlreadyExistsError(id: EventId) extends Exception(s"Event '${id.value }' already exists")
