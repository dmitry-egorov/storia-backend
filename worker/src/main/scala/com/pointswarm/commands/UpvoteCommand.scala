package com.pointswarm.commands

import com.pointswarm.common.dtos._

case class UpvoteCommand(reportId: ReportId, voterId: ProfileId)
{
    assert(reportId != null)
    assert(voterId != null)
}
