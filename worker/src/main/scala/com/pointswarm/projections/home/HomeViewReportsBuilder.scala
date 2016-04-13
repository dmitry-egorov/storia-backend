package com.pointswarm.projections.home

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.HtmlContent
import com.pointswarm.domain.common.EventIdAgg
import com.pointswarm.domain.reporting.Report
import com.pointswarm.domain.reporting.Report._
import com.pointswarm.projections.common.AuthorViewLoader
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent._
import scala.concurrent.duration._

class HomeViewReportsBuilder(fb: Firebase, authorLoader: AuthorViewLoader)(implicit f: Formats, ec: ExecutionContext) extends Projection[Report.type]
{
    private lazy val homeRef: Firebase = fb / "home"
    private def eventRefOf(eventId: EventIdAgg): Firebase = homeRef / eventId.hash
    private def previewRefOf(eventId: EventIdAgg): Firebase = eventRefOf(eventId) / "preview"

    def project(reportId: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        event match
        {
            case Added(content) =>
                for
                {
                    finalReportsCount <- updateReportsCount(reportId)
                    newPreview = finalReportsCount == 1
                    _ <- trySetPreview(reportId, content, newPreview)
                }
                yield s"Home view: updated event's '${reportId.eventId }' report count to '$finalReportsCount'" +
                      (if (newPreview) s" and preview to report of '${reportId.authorId }'" else "")
            case Edited(content) =>
                for
                {
                    currentPreviewIdHash <- getCurrentPreviewIdHash(reportId.eventId)
                    isPreview = currentPreviewIdHash == reportId.hash
                    _ <- tryUpdatePreviewContent(reportId, isPreview, content)
                }
                yield
                    if (isPreview) s"Home view: updated preview of ${reportId.eventId } with report of '${
                        reportId.authorId
                    }'"
                    else s"Home view: '$reportId' is not a preview, nothing to update"
        }
    }

    private def getCurrentPreviewIdHash(eventId: EventIdAgg): Future[String] =
    {
        (previewRefOf(eventId) / "id").awaitValue[String](10 seconds)
    }

    private def updateReportsCount(id: Id): Future[Int] =
    {
        (eventRefOf(id.eventId) / "reportsCount")
        .transaction[Int](c => Some(c.getOrElse(0) + 1))
        .map(tr => tr.finalData.get)
    }

    private def trySetPreview(id: Id, content: HtmlContent, newPreview: Boolean): Future[Unit] =
    {
        if (newPreview) updatePreview(id, content)
        else Future.successful(())
    }

    private def updatePreview(id: Id, content: HtmlContent): Future[Unit] =
    {
        val eventId = id.eventId
        val previewRef = previewRefOf(eventId)

        val f1 = previewRef / "id" <-- id.hash
        val f2 = previewRef / "content" <-- content
        val f3 =
            for
            {
                author <- authorLoader.loadAuthor(id.authorId)
                _ <- previewRef / "author" <-- author
            }
            yield ()

        Seq(f1, f2, f3).whenAll.map(_ => ())
    }

    private def tryUpdatePreviewContent(reportId: Id, isPreview: Boolean, content: HtmlContent): Future[Unit] =
    {
        if (isPreview) updateContent(reportId.eventId, content)
        else Future.successful(())
    }

    def updateContent(eventId: EventIdAgg, content: HtmlContent): Future[Unit] =
    {
        (previewRefOf(eventId) / "content" <-- content).map(_ => ())
    }
}


