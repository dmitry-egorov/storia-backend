package com.pointswarm.minions.voter

import com.firebase.client.Firebase
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.hellfire.Extensions._
import org.json4s.Formats

import scala.concurrent._

class Voter(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[UpvoteCommand]
{
    def execute(commandId: CommandId, command: UpvoteCommand): Future[AnyRef] =
    {
        val upvotedRef = getUpvotedRef(command.reportId, command.profileId)
        for
        {
            upvoted <- upvotedRef.exists
            _ <-
            if (upvoted)
                upvotedRef.remove
            else
                upvotedRef.set(true: java.lang.Boolean)
        }
        yield SuccessResponse
    }

    def getUpvotedRef(reportId: ReportId, profileId: String): Firebase =
    {
        fb
        .child("reports")
        .child(reportId)
        .child("upvotedBy")
        .child(profileId)
    }
}








