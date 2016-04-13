package com.pointswarm.projections.home

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.domain.common.EventIdAgg
import com.pointswarm.domain.voting.Upvote
import com.pointswarm.domain.voting.Upvote._
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent._
import scala.concurrent.duration._

class HomeViewUpvotesBuilder(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Projection[Upvote.type]
{
    private lazy val homeRef: Firebase = fb / "home"
    private def eventRefOf(eventId: EventIdAgg): Firebase = homeRef / eventId
    private def previewRefOf(eventId: EventIdAgg): Firebase = eventRefOf(eventId) / "preview"

    def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        val casted = event match
        {
            case Casted    => +1
            case Cancelled => -1
        }

        val eventId = id.reportId.eventId
        for
        {
            currentPreviewHash <- (previewRefOf(eventId) / "id").awaitValue[String](10 seconds)
            r <-
            if (currentPreviewHash == id.reportId.hash)
                updateUpvote(eventId, casted)
                .map(total => s"Home view: event's '$eventId' preview upvote updated by '$casted' to '$total'")
            else Future.successful(s"Home view: upvoted report '$id' is not in preview")
        }
            yield r
    }

    def updateUpvote(eventId: EventIdAgg, increment: Int): Future[Int] =
    {
        (previewRefOf(eventId) / "upvotes")
        .transaction[Int](c => Some(c.getOrElse(0) + increment))
        .map(tr => tr.finalData.get)
    }
}
