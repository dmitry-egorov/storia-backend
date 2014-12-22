package com.pointswarm.minions.voter

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.fireLegion._
import com.pointswarm.fireLegion.messenger.MessengerExtensions._
import com.pointswarm.fireLegion.messenger.SuccessResponse
import org.json4s.Formats

import scala.concurrent._

class Voter(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[UpvoteCommand] {
    def execute(commandId: CommandId, command: UpvoteCommand): Future[AnyRef] = {
        val reportId = command.reportId
        val voterId = command.voterId

        for {
            u <- hasUpvoted(reportId, voterId)
            _ <- updateUpvoted(reportId, voterId, u)
            _ <- sortReports(reportId)
        }
        yield SuccessResponse
    }

    private def updateUpvoted(reportId: ReportId, voterId: ProfileId, upvoted: Boolean): Future[String] = {
        if (upvoted) removeUpvoted(reportId, voterId) else setUpvoted(reportId, voterId)
    }

    private def hasUpvoted(reportId: ReportId, voterId: ProfileId): Future[Boolean] = {
        upvotedRoot(reportId, voterId).exists
    }

    private def setUpvoted(reportId: ReportId, voterId: ProfileId): Future[String] = {
        upvotedRoot(reportId, voterId) <-- true
    }

    private def removeUpvoted(reportId: ReportId, voterId: ProfileId): Future[String] = {
        upvotedRoot(reportId, voterId).remove
    }

    private def upvotedRoot(reportId: ReportId, profileId: String): Firebase =
        fb / "reports" / reportId / "upvotedBy" / profileId

    private def sortReports(reportId: ReportId): Future[Option[AnyRef]] = {
        val command = SortReportsCommand(None, Some(reportId))
        fb.request[SortReportsCommand]("reportsSorter", command)
    }
}