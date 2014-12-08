package com.pointswarm.searcher

import com.firebase.client.Firebase
import com.ning.http.client.Response
import com.pointswarm.common._
import com.pointswarm.processing.FirebaseCommandProcessor
import com.pointswarm.searcher.SearchResponse.EventIdsEx
import com.pointswarm.serialization.CommonFormats
import com.pointswarm.wabisabi.DSL._
import rx.lang.scala.Subscription
import wabisabi.Client

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
        .term("text", queryText)
        .map(toResponse)
    }

    private def toResponse(elasticResponse: Response): SearchResponse =
    {
        elasticResponse
        .hits[TextIndexEntry]
        .map(_.eventId)
        .distinct
        .toSearchResponse
    }
}



