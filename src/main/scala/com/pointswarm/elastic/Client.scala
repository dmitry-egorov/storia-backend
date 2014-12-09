package com.pointswarm.elastic

import java.nio.charset.StandardCharsets

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

    def index(indexName: String, indexType: String): IndexDefinition = new IndexDefinition(indexName, indexType)

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
                .setBody( s"""{"query" : {"term" : { "$termName" : "$queryText" }}}""".getBytes(StandardCharsets.UTF_8))
            }
            .ensureOk
            .map(_.hits[T])
    }

    class IndexDefinition(indexName: String, indexType: String)
    {
        def create() =
        {
            Http
            {
                baseUrl / indexName
            }
            //.ensureOk
            .map(_ => ())
        }

        def doc(doc: AnyRef)(implicit f: Formats): Future[Unit] =
            Http
            {
                (baseUrl / indexName / indexType)
                .setBody(doc.toJson)
            }
            .ensureOk
            .map(_ => ())
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
            {
                response
            }
            else
            {
                val code = response.getStatusCode
                val text = response.getStatusText
                val body = response.getResponseBody

                throw new RuntimeException(s"$code ($text): $body")
            }
    }

    private case class ElasticResponse[T](hits: HitsList[T])

    private case class HitsList[T](hits: List[Hit[T]])

    private case class Hit[T](_source: T)

}