package com.pointswarm.minions.eventViewGenerator

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.fireLegion._
import com.pointswarm.fireLegion.messenger.SuccessResponse
import org.json4s.Formats

import scala.concurrent._

class EventViewGenerator(root: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[AddEventCommand]
{
    private lazy val eventsRoot = root / "events"

    def execute(commandId: CommandId, command: AddEventCommand): Future[AnyRef] =
    {
        val title = command.title
        val id = EventId(title.alias)

        for
        {
            exists <- exists(id)
            _ <- generateEventView(title, id, exists)
        }
        yield SuccessResponse
    }

    private def exists(eventId: EventId): Future[Boolean] =
    {
        eventsRoot / eventId exists
    }

    private def generateEventView(title: String, id: EventId, exists: Boolean): Future[String] =
    {
        if (exists) throw EventAlreadyExistsError(id)

        val view = EventView(title)

        eventsRoot / id <-- view
    }
}




