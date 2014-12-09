package com.pointswarm.searcher

import com.firebase.client.Firebase
import com.pointswarm.common._
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.processing.FirebaseCommandProcessor
import com.pointswarm.searcher.SearchResponse.EventIdsEx
import org.json4s.Formats
import rx.lang.scala.Subscription

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Searcher
{
    implicit val formats = CommonFormats.formats

    def run(fb: Firebase, elastic: Client): Subscription =
    {
        val searcher = new Searcher(fb, elastic)
        FirebaseCommandProcessor.run(fb.child("commands").child("search"), "search", searcher.elasticSearch)
    }
}

class Searcher(fb: Firebase, elastic: Client)(implicit f: Formats)
{

    def elasticSearch(command: SearchCommand): Future[SearchResponse] =
    {
        val queryText = command.query.toLowerCase

        elastic
        .search("texts")
        .term[TextIndexEntry]("text", queryText)
        .map(toResponse)
    }

    private def toResponse(elasticResponse: List[TextIndexEntry]): SearchResponse =
    {
        elasticResponse
        .map(_.eventId)
        .distinct
        .toSearchResponse
    }
}



