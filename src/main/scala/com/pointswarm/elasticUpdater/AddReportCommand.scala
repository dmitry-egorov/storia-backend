package com.pointswarm.elasticUpdater

import com.pointswarm.common._

case class AddReportCommand(content: String, eventId: EventId, authorId: ProfileId)
