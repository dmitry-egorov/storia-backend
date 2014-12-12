package com.pointswarm.minions.searcher

import com.pointswarm.common._

case class SearcherResponse(eventIds: List[EventId])

object SearcherResponse
{
    implicit class EventIdsEx(eventIds: List[EventId])
    {
        def toSearchResponse = new SearcherResponse(eventIds)
    }
}
