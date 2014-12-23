package com.pointswarm.minions.searcher

import com.dmitryegorov.tools.elastic._
import com.pointswarm.commands._
import com.pointswarm.common.views._
import com.pointswarm.fireLegion._
import com.pointswarm.migration.Migrator
import com.pointswarm.minions.searcher.SearcherResponse._
import org.json4s.Formats

import scala.concurrent._

class Searcher(elastic: Client)(implicit f: Formats, ec: ExecutionContext) extends Minion[SearchCommand]
{

    override def prepare: Future[Unit] = Migrator.createTextIndex(elastic)

    def execute(commandId: CommandId, command: SearchCommand): Future[SearcherResponse] =
    {
        val queryText = command.query.toLowerCase

        (elastic search "texts")
        .`match`[TextIndexEntryView]("text", queryText)
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








