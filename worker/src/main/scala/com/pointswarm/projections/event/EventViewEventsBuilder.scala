package com.pointswarm.projections.event

import com.dmitryegorov.tools.extensions.SanitizeExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.domain.common.EventIdAgg
import com.pointswarm.domain.reporting.Event
import com.pointswarm.domain.reporting.Event._
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class EventViewEventsBuilder(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Projection[Event.type]
 {
     private lazy val eventsRef: Firebase = fb / "events"
     private def eventRefOf(eventId: EventIdAgg): Firebase = eventsRef / eventId.value.sanitize

     override def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
     {
         val title = event match
         {
             case Created(t) => t
         }

         (eventRefOf(id) / "title" <-- title).map(_ => s"Event view '$id': created '$title'")
     }
 }
