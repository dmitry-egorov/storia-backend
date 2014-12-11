package com.pointswarm.minions.search

import com.firebase.client.Firebase
import com.pointswarm.application.migration.Migrator
import com.pointswarm.common._
import com.pointswarm.minions.search.SearcherResponse._
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.processing._
import org.json4s.Formats

import scala.concurrent._

class Searcher(fb: Firebase, elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Minion[SearchCommand]
{

    def execute(command: SearchCommand): Future[SearcherResponse] =
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

    override def prepare: Future[Unit] = Migrator.createTextIndex(elastic)
}

case class SearcherResponse(eventIds: List[EventId])

object SearcherResponse
{
    implicit class EventIdsEx(eventIds: List[EventId])
    {
        def toSearchResponse = new SearcherResponse(eventIds)
    }
}





