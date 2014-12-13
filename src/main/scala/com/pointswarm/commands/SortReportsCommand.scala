package com.pointswarm.commands

import com.pointswarm.common.dtos._

case class SortReportsCommand(eventId: EventId)
{
    assert(eventId != null)
}
