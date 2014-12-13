package com.pointswarm.application

import com.firebase.client.Firebase
import com.pointswarm.common.format._
import com.pointswarm.minions.eventStretcher._
import com.pointswarm.minions.eventViewGenerator.EventViewGenerator
import com.pointswarm.minions.reportStreacher.ReportStretcher
import com.pointswarm.minions.reportViewGenerator._
import com.pointswarm.minions.reportsSorter._
import com.pointswarm.minions.searcher.Searcher
import com.pointswarm.minions.voter._
import com.pointswarm.tools.elastic.Client
import com.pointswarm.tools.fireLegion.ArmyAnnouncer._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.futuristic.cancellation.CancellationSource

import scala.concurrent._
import scala.concurrent.duration._

object WorkerApp extends App
{
    println("A new Master is born...")

    implicit val formats = CommonFormats.formats
    implicit val executionContext = ExecutionContext.Implicits.global

    val cancellation = new CancellationSource

    val fb = new Firebase(WorkerConfig.fbUrl)
    val elastic = new Client(WorkerConfig.elasticUrl)

    val searcher = new Searcher(fb, elastic)
    val elasticAddEvent = new EventStretcher(fb, elastic)
    val elasticAddReport = new ReportStretcher(fb, elastic)
    val eventViewGenerator = new EventViewGenerator(fb)
    val reportViewGenerator = new ReportViewGenerator(fb)
    val reportsSorter = new ReportsSorter(fb)
    val voter = new Voter(fb)

    val army =
        Master(fb)
        .recruitDistributor
        .recruit(searcher)
        .recruit(elasticAddEvent)
        .recruit(elasticAddReport)
        .recruit(eventViewGenerator)
        .recruit(reportViewGenerator)
        .recruit(reportsSorter)
        .recruit(voter)
        .createArmy.withAnnouncer

    val conquest =
        for
        {
            _ <- army.prepare
            _ <- army.conquer(cancellation)
        } yield ()

    sys addShutdownHook
    {
        cancellation.cancel()

        Await.result(conquest, 10 seconds)
    }

    Await.result(conquest, Duration.Inf)
}

