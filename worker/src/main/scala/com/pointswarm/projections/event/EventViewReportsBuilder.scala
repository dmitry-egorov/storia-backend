package com.pointswarm.projections.event

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.Alias
import com.pointswarm.domain.reporting.Report
import com.pointswarm.domain.reporting.Report._
import com.pointswarm.projections.common.{AuthorViewLoader, EventAliasStorage, ProfileAliasStorage}
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent._

class EventViewReportsBuilder(fb: Firebase, eventAliasStorage: EventAliasStorage, profileAliasStorage: ProfileAliasStorage, authorLoader: AuthorViewLoader)(implicit f: Formats, ec: ExecutionContext) extends Projection[Report.type]
{
    private lazy val eventsRef: Firebase = fb / "events"
    private def eventRefOf(alias: Alias): Firebase = eventsRef / alias
    private def reportRefOf(alias: Alias, profileAlias: Alias): Firebase = eventRefOf(alias) / "reports" / profileAlias

    def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        val content = event match
        {
            case Added(c)  => c
            case Edited(c) => c
        }

        val eventId = id.eventId
        val authorId = id.authorId
        val f1 = eventAliasStorage.getAliasOf(eventId)
        val f2 = profileAliasStorage.getAliasOf(authorId)
        val f3 = authorLoader.loadAuthor(authorId)

        for
        {
            eventAlias <- f1
            profileAlias <- f2
            f4 = reportRefOf(eventAlias, profileAlias) / "content" <-- content
            author <- f3
            f5 = reportRefOf(eventAlias, profileAlias) / "author" <-- author
            _ <- f4
            _ <- f5
        }
        yield s"Event view '$eventId': content of report of '$authorId' updated to '$content'."
    }
}


