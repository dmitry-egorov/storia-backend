package com.pointswarm.projections.event

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.Alias
import com.pointswarm.domain.reporting.Event
import com.pointswarm.domain.reporting.Event._
import com.pointswarm.projections.common.EventAliasStorage
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class EventViewEventsBuilder(fb: Firebase, aliasStorage: EventAliasStorage)(implicit f: Formats, ec: ExecutionContext)
    extends Projection[Event.type]
{
    private lazy val eventsRef: Firebase = fb / "events"
    private def eventRefOf(alias: Alias): Firebase = eventsRef / alias

    override def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        val title = event match
        {
            case Created(t) => t
        }

        for
        {
            alias <- aliasStorage.getAliasOf(id)
            f1 = eventRefOf(alias) / "title" <-- title
            f2 = eventRefOf(alias) / "id" <-- id
            _ <- f1
            _ <- f2
        }
        yield s"Event view '$alias': created '$title'"
    }
}
