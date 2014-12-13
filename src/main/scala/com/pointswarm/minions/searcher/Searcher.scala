package com.pointswarm.minions.searcher

import com.firebase.client.Firebase
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.migration.Migrator
import com.pointswarm.commands._
import com.pointswarm.common._
import com.pointswarm.minions.searcher.SearcherResponse._
import com.pointswarm.tools.elastic._
import com.pointswarm.tools.fireLegion._
import org.json4s.Formats

import scala.concurrent._

class Searcher(fb: Firebase, elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Minion[SearchCommand]
{

    override def prepare: Future[Unit] = Migrator.createTextIndex(elastic)

    def execute(commandId: CommandId, command: SearchCommand): Future[SearcherResponse] =
    {
        val queryText = command.query.toLowerCase

        elastic
        .search("texts")
        .term[TextIndexEntryView]("text", queryText)
        .map(toResponse)
    }

    private def toResponse(elasticResponse: List[TextIndexEntryView]): SearcherResponse =
    {
        elasticResponse
        .map(_.eventId)
        .distinct
        .toSearchResponse
    }
}








