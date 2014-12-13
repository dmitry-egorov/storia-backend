package com.pointswarm.minions.reportViewGenerator

import java._

import com.firebase.client._
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.fireLegion.messenger.MessengerExtensions._
import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.hellfire.Extensions._
import org.joda.time._
import org.json4s._

import scala.collection.immutable._
import scala.concurrent._

class ReportViewGenerator(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[ReportCommand]
{
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
        val reportId = new ReportId(fb.newKey)
        val view = new ReportView(content, addedOn, authorId, eventId, Set.empty)

        val reportsFuture = setReportView(view, reportId)
        val profileReportsFuture = setProfileReports(eventId, authorId, reportId)
        val eventReportsFuture = setEventReports(eventId, reportId)
        val setHistoryFuture = setHistory(content, reportId)
        val sorterFuture = sortReports(eventId)

        List(
            reportsFuture,
            profileReportsFuture,
            eventReportsFuture,
            setHistoryFuture,
            sorterFuture
        )
        .waitAll
    }

    private def updateReport(id: ReportId, content: HtmlContent, time: DateTime): Future[Unit] =
    {
        val contentFuture = setContent(id, content)
        val historyFuture = setHistory(content, id)

        List(contentFuture, historyFuture).waitAll
    }

    private def setContent(id: ReportId, content: HtmlContent): Future[String] =
    {
        fb.child("reports").child(id).child("content").set(content)
    }

    private def setHistory(content: HtmlContent, reportId: ReportId): Future[String] =
    {
        val history = HistoricalContent(content)
        fb.child("reportHistories").child(reportId).push(history)
    }

    private def setReportView(view: ReportView, reportId: ReportId): Future[String] =
    {
        fb.child("reports").child(reportId).set(view)
    }

    private def sortReports(eventId: EventId): Future[Option[AnyRef]] =
    {
        val command = new SortReportsCommand(eventId)
        fb.request[SortReportsCommand]("reportsSorter", command)
    }

    private def setProfileReports(eventId: EventId, authorId: ProfileId, reportId: ReportId): Future[String] =
    {
        fb
        .child("profileReports")
        .child(authorId)
        .child(eventId)
        .set(reportId: String)
    }

    private def setEventReports(eventId: EventId, reportId: ReportId): Future[String] =
    {
        fb
        .child("events")
        .child(eventId)
        .child("reports")
        .child(reportId)
        .set(true: lang.Boolean)
    }

    private def getReportIdOf(eventId: EventId, authorId: ProfileId): Future[Option[ReportId]] =
    {
        fb
        .child("profileReports")
        .child(authorId)
        .child(eventId)
        .value[ReportId]
    }
}
