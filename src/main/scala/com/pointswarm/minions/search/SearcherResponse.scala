package com.pointswarm.minions.search

import com.pointswarm.common.EventId

case class SearcherResponse(eventIds: List[EventId])

object SearcherResponse
{
    implicit class EventIdsEx(eventIds: List[EventId])
    {
        def toSearchResponse = new SearcherResponse(eventIds)
    }
}