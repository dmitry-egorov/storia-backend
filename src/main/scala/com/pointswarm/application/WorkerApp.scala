package com.pointswarm.application

import com.firebase.client.Firebase
import com.pointswarm.common.CommonFormats
import com.pointswarm.minions.addEvent.{EventStretcher, EventViewGenerator}
import com.pointswarm.minions.report.ReportStretcher
import com.pointswarm.minions.search.Searcher
import com.pointswarm.tools.elastic.Client
import com.pointswarm.tools.futuristic.cancellation.CancellationSource
import com.pointswarm.tools.processing.Master

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

            val searcher = new Searcher(fb, elastic)
            val elasticAddEvent = new EventStretcher(fb, elastic)
            val elasticAddReport = new ReportStretcher(fb, elastic)
            val eventViewGenerator = new EventViewGenerator(fb)

            val commandsRef = fb.child("commands")

            val army =
                Master()
                .recruit(searcher)
                .recruit(elasticAddEvent)
                .recruit(elasticAddReport)
                .recruit(eventViewGenerator)
                .createArmy(commandsRef, cancellation)

            println("Preparing...")

            await(army.prepare)

            println("Awaiting commands to conquer the world...")

            await(army.conquer)
        }

    sys addShutdownHook
    {
        println("Disbanding...")
        cancellation.cancel()

        val total = Await.result(run, 10 seconds)

        println(s"Finished. Executed $total commands.")
    }

    Await.result(run, Duration.Inf)
}