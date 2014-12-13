package com.pointswarm.minions.reportsSorter

import com.firebase.client._
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.hellfire.Extensions._
import org.json4s._

import scala.concurrent._
import scala.concurrent.duration._

class ReportsSorter(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[SortReportsCommand]
{
    def execute(commandId: CommandId, command: SortReportsCommand): Future[AnyRef] =
    {
        val eventId = command.eventId

        getReportIdsOf(eventId)
        .flatMap(reportIds => getReports(reportIds))
        .flatMap(reports => setBestReport(eventId, reports))
        .map(_ => SuccessResponse)
    }

    def setBestReport(eventId: EventId, reports: List[(ReportId, ReportView)]): Future[String] =
    {
        val maxId = findBestReport(reports)
        setPreviewReport(eventId, maxId)
    }

    def setPreviewReport(eventId: EventId, previewId: ReportId): Future[String] =
    {
        getEventRef(eventId)
        .child("previewId")
        .set(previewId: String)
    }

    def findBestReport(reports: List[(ReportId, ReportView)]): ReportId =
    {
        reports.maxBy(r => r._2.upvotedBy.count(_ => true))._1
    }

    def getReports(reportIds: List[ReportId]): Future[List[(ReportId, ReportView)]] =
    {
        reportIds
        .map(id => getReport(id))
        .whenAll
    }

    def getReport(id: ReportId): Future[(ReportId, ReportView)] =
    {
        fb
        .child("reports")
        .child(id)
        .awaitValue[ReportView]
        .timeout(5 seconds)
        .map(r => (id, r))
    }

    def getReportIdsOf(eventId: EventId): Future[List[ReportId]] =
    {
        getEventRef(eventId)
        .child("reports")
        .awaitValue[Map[ReportId, Boolean]]
        .timeout(5 seconds)
        .map(m => m.keys.toList)
    }

    def getEventRef(eventId: EventId): Firebase =
    {
        fb
        .child("events")
        .child(eventId)
    }
}
