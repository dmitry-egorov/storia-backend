package com.pointswarm.minions.addReport

import com.pointswarm.common.{EventId, ProfileId}

case class ReportCommand(eventId: EventId, authorId: ProfileId, content: String)
