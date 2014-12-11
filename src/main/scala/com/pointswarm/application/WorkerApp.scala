package com.pointswarm.application

import java.util.concurrent.CancellationException

import com.firebase.client.Firebase
import com.pointswarm.application.migration.Migrator
import com.pointswarm.common.CommonFormats
import com.pointswarm.minions.addEvent.EventStretcher
import com.pointswarm.minions.addReport.ReportStretcher
import com.pointswarm.minions.search.Searcher
import com.pointswarm.tools.futuristic.cancellation.CancellationSource
import com.pointswarm.tools.elastic.Client
import com.pointswarm.tools.processing.FireMaster

import scala.async.Async._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object WorkerApp extends App
{
    println("Worker started...")

    val cancellation = new CancellationSource
    implicit val formats = CommonFormats.formats

    val run =
        async
        {
            val fb = new Firebase(WorkerConfig.fbUrl)
            val elastic = new Client(WorkerConfig.elasticUrl)

            await(Migrator.migrate(elastic))
            println("Migration complete...")

            val searcher = new Searcher(fb, elastic)
            val elasticAddEvent = new EventStretcher(fb, elastic)
            val elasticAddReport = new ReportStretcher(fb, elastic)

            val run =
                FireMaster
                .create()
                .subdue(searcher)
                .subdue(elasticAddEvent)
                .subdue(elasticAddReport)
                .run(fb.child("commands"), cancellation)

            println("Listening...")
            await(run)
        }
        .recover
        {
            case ce: CancellationException =>
        }

    sys addShutdownHook
    {
        println("Shutting down...")
        cancellation.cancel()

        val total = Await.result(run.recover{ case e => 0 }, 10 seconds)

        println(s"Finished. Processed $total events")
    }

    Await.result(run, Duration.Inf)
}