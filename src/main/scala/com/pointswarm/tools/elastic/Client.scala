package com.pointswarm.tools.elastic

import java.nio.charset.StandardCharsets

import com.netaporter.uri.Uri
import com.ning.http.client.Response
import com.pointswarm.tools.elastic.Client._
import com.pointswarm.tools.extensions.SerializationExtensions._
import dispatch.{Http, url}
import org.json4s.Formats

import scala.concurrent._

class Client(uri: String)(implicit ec: ExecutionContext)
{
    private lazy val baseUrl =
    {
        val withoutAuth = url(uri)

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

    private lazy val postUrl = baseUrl.POST.setContentType("application/json", "UTF-8")


    def search(indexName: String): SearchDefinition = new SearchDefinition(indexName)

    def index(indexName: String): IndexDefinition = new IndexDefinition(indexName)


    class SearchDefinition(indexName: String)
    {
        def term[T](termName: String, queryText: String)(implicit m: Manifest[T], f: Formats): Future[List[T]] =
            Http
            {
                (postUrl / indexName / "_search")
                .setBody( s"""{"query" : {"term" : { "$termName" : "$queryText" }}}""".getBytes(StandardCharsets.UTF_8))
            }
            .ensureOk
            .map(_.hits[T])
    }

    class IndexDefinition(indexName: String)
    {
        def create(): Future[Response] =
        {
            Http
            {
                postUrl / indexName
            }
            .ensureOk
        }

        def doc(indexType: String, id: String, doc: AnyRef)(implicit f: Formats): Future[Response] =
        {
            Http
            {
                (postUrl / indexName / indexType / id)
                .setBody(doc.toJson)
            }
            .ensureOk
        }

        def exists: Future[Boolean] =
        {
            Http
            {
                (baseUrl / indexName )
                .HEAD
            }
            .map(_.getStatusCode == 200)
        }
    }
}

object Client
{

    implicit class ResponseFutureEx(response: Future[Response])(implicit ec: ExecutionContext)
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