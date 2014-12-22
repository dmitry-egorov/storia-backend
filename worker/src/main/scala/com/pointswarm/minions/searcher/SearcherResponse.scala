package com.pointswarm.minions.searcher

import com.pointswarm.common.dtos._

case class SearcherResponse(eventIds: List[EventId])

object SearcherResponse {
    implicit class EventIdsEx(val eventIds: List[EventId]) extends AnyVal {
        def toSearchResponse = SearcherResponse(eventIds)
    }
}
