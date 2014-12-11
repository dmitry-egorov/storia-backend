package com.pointswarm.minions.report

import com.pointswarm.common.{EventId, ProfileId}

case class ReportCommand(eventId: EventId, authorId: ProfileId, content: String)
