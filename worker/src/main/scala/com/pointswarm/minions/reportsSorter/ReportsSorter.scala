package com.pointswarm.minions.reportsSorter

import com.firebase.client._
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.fireLegion._
import com.pointswarm.fireLegion.messenger.SuccessResponse
import com.dmitryegorov.tools.futuristic.FutureExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import org.json4s._

import scala.concurrent._
import scala.concurrent.duration._

class ReportsSorter(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[SortReportsCommand]
{
    private lazy val reportsRoot: Firebase = fb / "reports"
    private lazy val eventsRoot: Firebase = fb / "events"

    def execute(commandId: CommandId, command: SortReportsCommand): Future[AnyRef] =
    {
        for
        {
            eventId <- findEventId(command)
            reportIds <- getReportIdsOf(eventId)
            reports <- getReports(reportIds)
            _ <- setBestReport(eventId, reports)
        }
        yield SuccessResponse
    }

    def getReportsEventId(id: ReportId): Future[EventId] =
    {
        (reportsRoot / id / "eventId").awaitValue[EventId](5 seconds)
    }

    def findEventId(command: SortReportsCommand): Future[EventId] =
    {
        command.eventId match
        {
            case Some(id) => Future.successful(id)
            case None     => getReportsEventId(command.reportId.get)
        }
    }

    def setBestReport(eventId: EventId, reports: Seq[(ReportId, ReportView)]): Future[String] =
    {
        val maxId = findBestReport(reports)
        setPreviewReport(eventId, maxId)
    }

    def setPreviewReport(eventId: EventId, previewId: ReportId): Future[String] =
    {
        eventsRoot / eventId / "previewId" <-- (previewId: String)
    }

    def findBestReport(reports: Seq[(ReportId, ReportView)]): ReportId =
    {
        reports.maxBy(r => countUpvotes(r))._1
    }

    def countUpvotes(r: (ReportId, ReportView)): Int =
    {
        r._2.upvotedBy.map(x => x.count(_ => true)).getOrElse(0)
    }

    def getReports(reportIds: Seq[ReportId]): Future[Seq[(ReportId, ReportView)]] =
    {
        reportIds map getReport whenAll
    }

    def getReport(id: ReportId): Future[(ReportId, ReportView)] =
    {
        (reportsRoot / id).awaitValue[ReportView](5 seconds).map(r => (id, r))
    }

    def getReportIdsOf(id: EventId): Future[Seq[ReportId]] =
    {
        (eventsRoot / id / "reports").awaitValue[Map[ReportId, Boolean]](5 seconds)
        .map(m => m.keys.toList)
    }
}
