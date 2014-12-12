package com.pointswarm.application

import com.firebase.client.Firebase
import com.pointswarm.common.CommonFormats
import com.pointswarm.minions.distributor._
import com.pointswarm.minions.eventViewGenerator.EventViewGenerator
import com.pointswarm.minions.eventStretcher._
import com.pointswarm.minions.reportStreacher.ReportStretcher
import com.pointswarm.minions.searcher.Searcher
import com.pointswarm.tools.elastic.Client
import com.pointswarm.tools.futuristic.cancellation.CancellationSource
import com.pointswarm.tools.processing.Master

import scala.async.Async._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object WorkerApp extends App
{
    println("A new Master is born...")

    val cancellation = new CancellationSource
    implicit val formats = CommonFormats.formats

    val run =
        async
        {
            val fb = new Firebase(WorkerConfig.fbUrl)
            val elastic = new Client(WorkerConfig.elasticUrl)

            val searcher = new Searcher(fb, elastic)
            val elasticAddEvent = new EventStretcher(fb, elastic)
            val elasticAddReport = new ReportStretcher(fb, elastic)
            val eventViewGenerator = new EventViewGenerator(fb)
            val distributor = new Distributor(fb)


            val army =
                Master()
                .recruit(searcher)
                .recruit(elasticAddEvent)
                .recruit(elasticAddReport)
                .recruit(eventViewGenerator)
                .recruit(distributor)
                .createArmy(fb)

            println("Preparing the army...")

            await(army.prepare)

            println("Ready to conquer the world. Awaiting your commands...")

            await(army.conquer(cancellation))
        }

    sys addShutdownHook
    {
        println("Retreating...")
        cancellation.cancel()

        val total = Await.result(run, 10 seconds)

        println(s"Finished. Executed $total commands.")
    }

    Await.result(run, Duration.Inf)
}