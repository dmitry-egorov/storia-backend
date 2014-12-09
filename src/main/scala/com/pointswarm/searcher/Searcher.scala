package com.pointswarm.searcher

import com.firebase.client.Firebase
import com.pointswarm.common._
import com.pointswarm.processing.FirebaseCommandProcessor
import com.pointswarm.searcher.SearchResponse.EventIdsEx
import com.pointswarm.serialization.CommonFormats
import com.pointswarm.elastic._
import rx.lang.scala.Subscription

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Searcher
{
    def run(fb: Firebase, elastic: Client) = new Searcher(fb, elastic).run()
}

class Searcher(fb: Firebase, elastic: Client)
{
    implicit val formats = CommonFormats.formats

    def run(): Subscription =
    {
        FirebaseCommandProcessor.run(fb, "search", elasticSearch)
    }

    private def elasticSearch(command: SearchCommand): Future[SearchResponse] =
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



