package com.pointswarm.elastic

import com.netaporter.uri.Uri
import com.ning.http.client.Response
import com.pointswarm.elastic.Client._
import com.pointswarm.extensions.SerializationExtensions._
import dispatch.{Http, url}
import org.json4s.Formats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Client(uri: String)
{
    val baseUrl = prepareUrl()

    def search(indexName: String): SearchDefinition = new SearchDefinition(indexName)

    def index(indexName: String): IndexDefinition = new IndexDefinition(indexName)

    private def prepareUrl() =
    {
        val withoutAuth =
            url(uri)
            .POST
            .setContentType("application/json", "UTF-8")

        val parsed = Uri.parse(uri)

        val withAuth =
            for
            {
                name <- parsed.user
                pass <- parsed.password
            }
            yield withoutAuth.as_!(name, pass)

        withAuth.getOrElse(withoutAuth)
    }

    class SearchDefinition(indexName: String)
    {
        def term[T](termName: String, queryText: String)(implicit m: Manifest[T], f: Formats): Future[List[T]] =
            Http
            {
                (baseUrl / indexName / "_search")
                .setBody( s"""{"query" : {"term" : { "$termName" : "$queryText" }}}""")
            }
            .ensureOk
            .map(_.hits[T])
    }

    class IndexDefinition(indexName: String)
    {
        def doc(doc: AnyRef)(implicit f: Formats): Future[Unit] =
            Http
            {
                (baseUrl / indexName / "t")
                .setBody(doc.toJson)
            }
            .ensureOk
            .map(_ => {})
    }

}

object Client
{

    implicit class ResponseFutureEx(response: Future[Response])
    {
        def ensureOk = response.map(_.assertOk())
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

        def assertOk() =
            if (200 until 299 contains response.getStatusCode)
                response
            else
                throw new RuntimeException(s"${response.getStatusCode } (${response.getStatusText }): ${
                    response.getResponseBody
                }")
    }

    private case class ElasticResponse[T](hits: HitsList[T])

    private case class HitsList[T](hits: List[Hit[T]])

    private case class Hit[T](_source: T)

}