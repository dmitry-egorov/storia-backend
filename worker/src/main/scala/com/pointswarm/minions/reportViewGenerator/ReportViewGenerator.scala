package com.pointswarm.minions.reportViewGenerator

import com.firebase.client._
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.fireLegion._
import com.pointswarm.fireLegion.messenger.MessengerExtensions._
import com.pointswarm.fireLegion.messenger.SuccessResponse
import com.dmitryegorov.tools.futuristic.FutureExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import org.joda.time._
import org.json4s._

import scala.collection.immutable._
import scala.concurrent._

class ReportViewGenerator(root: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[ReportCommand]
{
    private lazy val reportsRoot: Firebase = root / "reports"
    private lazy val profileReportsRoot = root / "profileReports"
    private lazy val eventsRoot = root / "events"
    private lazy val reportHistoriesRoot: Firebase = root / "reportHistories"

    def execute(commandId: CommandId, command: ReportCommand): Future[AnyRef] =
    {
        val content = command.content
        val eventId = command.eventId
        val authorId = command.authorId
        val addedOn = command.addedOn

        getReportIdOf(eventId, authorId)
        .flatMap
        {
            case Some(reportId) => updateReport(reportId, content, addedOn)
            case None           => addReport(content, eventId, authorId, addedOn)
        }
        .map(x => SuccessResponse)
    }

    def addReport(content: HtmlContent, eventId: EventId, authorId: ProfileId, addedOn: DateTime): Future[Unit] =
    {
        val reportId = ReportId(root.newKey)
        val view = ReportView(content, addedOn, authorId, eventId, None)

        List(
            setReportView(view, reportId),
            setProfileReports(eventId, authorId, reportId),
            setEventReports(eventId, reportId),
            setHistory(reportId, content),
            sortReports(eventId)
        )
        .waitAll
    }

    private def updateReport(id: ReportId, content: HtmlContent, time: DateTime): Future[Unit] =
    {
        List(
            setContent(id, content),
            setHistory(id, content)
        ).waitAll
    }

    private def setContent(id: ReportId, content: HtmlContent): Future[String] =
    {
        reportsRoot / id / "content" <-- content
    }


    private def setHistory(reportId: ReportId, content: HtmlContent): Future[String] =
    {
        reportHistoriesRoot / reportId <%- HistoricalContent(content)
    }

    private def setReportView(view: ReportView, reportId: ReportId): Future[String] =
    {
        reportsRoot / reportId <-- view
    }

    private def sortReports(eventId: EventId): Future[Option[AnyRef]] =
    {
        val command = SortReportsCommand(Some(eventId), None)
        root request("reportsSorter", command)
    }

    private def setProfileReports(eventId: EventId, authorId: ProfileId, reportId: ReportId): Future[String] =
    {
        profileReportsRoot / authorId / eventId <-- reportId
    }

    private def setEventReports(eventId: EventId, reportId: ReportId): Future[String] =
    {
        eventsRoot / eventId / "reports" / reportId <-- true
    }

    private def getReportIdOf(eventId: EventId, authorId: ProfileId): Future[Option[ReportId]] =
    {
        (profileReportsRoot / authorId / eventId).value[ReportId]
    }
}
