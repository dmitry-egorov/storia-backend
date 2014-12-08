package com.pointswarm.wabisabi

import com.ning.http.client.Response
import com.pointswarm.extensions.SerializationExtensions._
import org.json4s.Formats
import wabisabi.Client

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object DSL
{

    implicit class ClientEx(client: Client)
    {
        def search(indexName: String): SearchDefinition = new SearchDefinition(indexName, client)

        def index(indexName: String): IndexDefinition = new IndexDefinition(indexName, client)
    }

    class SearchDefinition(indexName: String, client: Client)
    {
        def term(termName: String, queryText: String): Future[Response] =
            client
            .search("texts", s"""{"query" : {"term" : { "$termName" : "$queryText" }}}""")
            .ensureOk
    }

    class IndexDefinition(indexName: String, client: Client)
    {
        def doc(doc: AnyRef)(implicit f: Formats) =
            client
            .index(indexName, indexName, None, doc.toJson)
            .ensureOk
    }

    implicit class ResponseFutureEx(response: Future[Response])
    {
        def ensureOk =
            response
            .map(x =>
                 {
                     x.assertOk()
                     x
                 })
    }

    implicit class ResponseEx(response: Response)
    {
        def hits[T](implicit m: Manifest[T], f: Formats): List[T] =
        {
            response
            .getResponseBody
            .readAs[ElasticResponse[T]]
            .hits
            .hits
            .map(_._source)
        }

        def assertOk() = if (!(200 until 299 contains response.getStatusCode)) throw new RuntimeException(response.getStatusText)
    }

    private case class ElasticResponse[T](hits: HitsList[T])

    private case class HitsList[T](hits: List[Hit[T]])

    private case class Hit[T](_source: T)

}
