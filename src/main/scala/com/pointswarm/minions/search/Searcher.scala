package com.pointswarm.minions.search

import com.firebase.client.Firebase
import com.pointswarm.common._
import com.pointswarm.minions.search.SearcherResponse._
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.processing._
import org.json4s.Formats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Searcher(fb: Firebase, elastic: Client)(implicit f: Formats) extends Minion[SearchCommand]
{

    def obey(command: SearchCommand): Future[SearcherResponse] =
    {
        val queryText = command.query.toLowerCase

        elastic
        .search("texts")
        .term[TextIndexEntry]("text", queryText)
        .map(toResponse)
    }

    private def toResponse(elasticResponse: List[TextIndexEntry]): SearcherResponse =
    {
        elasticResponse
        .map(_.eventId)
        .distinct
        .toSearchResponse
    }
}

case class SearcherResponse(eventIds: List[EventId])

object SearcherResponse
{
    implicit class EventIdsEx(eventIds: List[EventId])
    {
        def toSearchResponse = new SearcherResponse(eventIds)
    }
}





