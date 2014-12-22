package com.pointswarm.common.views

import com.pointswarm.common.dtos._

case class TextIndexEntryView(eventId: EventId, text: String) {
    assert(eventId != null)
    assert(text != null && text.trim.nonEmpty)
}
