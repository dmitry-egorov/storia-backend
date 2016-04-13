package com.pointswarm.projections.event

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.Alias
import com.pointswarm.domain.voting.Upvote
import com.pointswarm.domain.voting.Upvote._
import com.pointswarm.projections.common.{EventAliasStorage, ProfileAliasStorage}
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class EventViewUpvotesBuilder(fb: Firebase, eventAliasStorage: EventAliasStorage, profileAliasStorage: ProfileAliasStorage)(implicit f: Formats, ec: ExecutionContext) extends Projection[Upvote.type]
{
    private lazy val eventsRef: Firebase = fb / "events"
    private def eventRefOf(alias: Alias): Firebase = eventsRef / alias
    private def reportRefOf(alias: Alias, authorAlias: Alias): Firebase = eventRefOf(alias) / "reports" / authorAlias

    def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        val casted = event match
        {
            case Casted    => +1
            case Cancelled => -1
        }

        val eventId = id.reportId.eventId
        val authorId = id.reportId.authorId

        val f1 = eventAliasStorage.getAliasOf(eventId)
        val f2 = profileAliasStorage.getAliasOf(authorId)
        for
        {
            eventAlias <- f1
            authorAlias <- f2
            total <- updateUpvote(eventAlias, authorAlias, casted)
        }
        yield s"Event view '$eventId': upvote of report of '$authorId' updated by '$casted' to $total"
    }

    def updateUpvote(eventAlias: Alias, authorAlias: Alias, increment: Int): Future[Int] =
    {
        (reportRefOf(eventAlias, authorAlias) / "upvotes")
        .transaction[Int](c => Some(c.getOrElse(0) + increment))
        .map(tr => tr.finalData.get)
    }
}
