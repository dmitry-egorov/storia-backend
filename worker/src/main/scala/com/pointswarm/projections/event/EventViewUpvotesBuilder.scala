package com.pointswarm.projections.event

import com.dmitryegorov.tools.extensions.SanitizeExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.domain.common.{ReportIdAgg, EventIdAgg}
import com.pointswarm.domain.voting.Upvote
import com.pointswarm.domain.voting.Upvote._
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class EventViewUpvotesBuilder(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Projection[Upvote.type]
 {
     private lazy val eventsRef: Firebase = fb / "events"
     private def eventRefOf(eventId: EventIdAgg): Firebase = eventsRef / eventId.value.sanitize
     private def reportRefOf(reportId: ReportIdAgg): Firebase = eventRefOf(reportId.eventId) / "reports" / reportId.authorId

     def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
     {
         val casted = event match
         {
             case Casted    => +1
             case Cancelled => -1
         }

         for
         {
             r <-
                 updateUpvote(id.reportId, casted)
                 .map(total => s"Event view '${id.reportId.eventId}': upvote of report of '${id.reportId.authorId}' updated by '$casted' to $total")
         }
             yield r
     }

     def updateUpvote(reportId: ReportIdAgg, increment: Int): Future[Int] =
     {
         (reportRefOf(reportId) / "upvotes")
         .transaction[Int](c => Some(c.getOrElse(0) + increment))
         .map(tr => tr.finalData.get)
     }
 }
