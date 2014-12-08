package com.pointswarm.searcher

import com.pointswarm.common.EventId

case class SearchResponse(eventIds: List[EventId])

object SearchResponse
{
    implicit class EventIdsEx(eventIds: List[EventId])
    {
        def toSearchResponse = new SearchResponse(eventIds)
    }
}