package com.pointswarm.minions.eventViewGenerator

import com.firebase.client.Firebase
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.tools.extensions.SanitizeExtensions._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.hellfire.Extensions._
import org.json4s.Formats

import scala.concurrent._

class EventViewGenerator(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[AddEventCommand]
{
    def execute(commandId: CommandId, command: AddEventCommand): Future[AnyRef] =
    {
        val title = command.title
        val id = new EventId(title.sanitize)

        val eventRef = fb.child("events").child(id.value)

        eventRef
        .exists
        .flatMap(
                exists =>
                {
                    if (exists)
                    {
                        throw new EventAlreadyExistsError(id)
                    }

                    val view = EventView.from(title)

                    eventRef.set(view)
                })
        .map(_ => SuccessResponse)
    }
}




