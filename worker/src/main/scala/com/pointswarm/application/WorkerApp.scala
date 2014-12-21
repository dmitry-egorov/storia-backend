package com.pointswarm.application

import com.dmitryegorov.hellfire.Hellfire._
import com.dmitryegorov.tools.elastic.Client
import com.dmitryegorov.tools.futuristic.cancellation.CancellationSource
import com.firebase.client.Firebase
import com.pointswarm.common.format._
import com.pointswarm.fireLegion.ArmyAnnouncer._
import com.pointswarm.fireLegion._
import com.pointswarm.minions.aggregator.ReportAggregator
import com.pointswarm.minions.eventStretcher._
import com.pointswarm.minions.eventViewGenerator.EventViewGenerator
import com.pointswarm.minions.paparazzi.Paparazzi
import com.pointswarm.minions.registrator.Registrator
import com.pointswarm.minions.reportStreacher.ReportStretcher
import com.pointswarm.minions.reportViewGenerator._
import com.pointswarm.minions.reportsSorter._
import com.pointswarm.minions.searcher.Searcher
import com.pointswarm.minions.voter._
import com.scalasourcing.backend.firebase.FirebaseEventStorage

import scala.concurrent._
import scala.concurrent.duration._

object WorkerApp extends App
{
    println("A new Master is born...")

    implicit val formats = CommonFormats.formats
    implicit val executionContext = ExecutionContext.Implicits.global

    val cancellation = new CancellationSource

    val fb = new Firebase(WorkerConfig.fbUrl)
    val es = new FirebaseEventStorage(fb / "es")
    val elastic = new Client(WorkerConfig.elasticUrl)

    val searcher = new Searcher(elastic)
    val elasticAddEvent = new EventStretcher(elastic)
    val elasticAddReport = new ReportStretcher(elastic)
    val eventViewGenerator = new EventViewGenerator(fb)
    val reportViewGenerator = new ReportViewGenerator(fb)
    val reportsSorter = new ReportsSorter(fb)
    val voter = new Voter(fb)
    val registrator = new Registrator(fb)
    val paparazzi = new Paparazzi(fb)
    val aggregator = new ReportAggregator(es)

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
        .recruit(registrator)
        .recruit(paparazzi)
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

