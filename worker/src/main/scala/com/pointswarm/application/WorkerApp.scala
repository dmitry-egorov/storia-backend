package com.pointswarm.application

import java.lang.System.err

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.futuristic.ObservableExtensions._
import com.dmitryegorov.futuristic.cancellation.CancellationSource
import com.dmitryegorov.hellfire.Hellfire._
import com.dmitryegorov.tools.elastic.Client
import com.dmitryegorov.tools.extensions.ThrowableExtensions._
import com.firebase.client.Firebase
import com.pointswarm.domain.reporting.{Event, Report}
import com.pointswarm.domain.voting.Upvote
import com.pointswarm.fireLegion.ArmyAnnouncer._
import com.pointswarm.fireLegion._
import com.pointswarm.minions.eventViewGenerator.EventViewGenerator
import com.pointswarm.minions.paparazzi.Paparazzi
import com.pointswarm.minions.registrator.Registrator
import com.pointswarm.minions.reportViewGenerator._
import com.pointswarm.minions.reportsSorter._
import com.pointswarm.minions.searcher.Searcher
import com.pointswarm.minions.voter._
import com.pointswarm.projections.event._
import com.pointswarm.projections.home._
import com.pointswarm.projections.search._
import com.scalasourcing.backend.firebase.FirebaseExecutorsBuilder

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object WorkerApp extends App
{
    println("A new Master is born...")

    implicit val formats = CommonFormats.formats
    implicit val executionContext = ExecutionContext.Implicits.global

    val cancellation = new CancellationSource

    val fb = new Firebase(WorkerConfig.fbUrl)

    val esRef = fb / "es"
    val viewsRef = fb / "views"
    val elastic = new Client(WorkerConfig.elasticUrl)

    val conquest = runMaster
    val executorRun = runExecutor

    val run = Seq(conquest, executorRun).waitAll

    sys addShutdownHook
    {
        cancellation.cancel()

        Await.result(conquest, 10 seconds)
    }

    Await.result(conquest, Duration.Inf)

    def runMaster: Future[Unit] =
    {
        val searcher = new Searcher(elastic)
        val eventViewGenerator = new EventViewGenerator(fb)
        val reportViewGenerator = new ReportViewGenerator(fb)
        val reportsSorter = new ReportsSorter(fb)
        val voter = new Voter(fb)
        val registrator = new Registrator(fb)
        val paparazzi = new Paparazzi(fb)

        val army =
            Master(fb)
            .recruitDistributor
            .recruit(searcher)
            .recruit(eventViewGenerator)
            .recruit(reportViewGenerator)
            .recruit(reportsSorter)
            .recruit(voter)
            .recruit(registrator)
            .recruit(paparazzi)
            .createArmy.withAnnouncer

        for
        {
            _ <- army.prepare
            _ <- army.conquer(cancellation)
        }
        yield ()
    }

    def runExecutor: Future[Unit] =
    {
        FirebaseExecutorsBuilder(esRef)
        .aggregate(Report)
        .aggregate(Upvote)
        .aggregate(Event)
        .projection(Event)(new EventStretcher(elastic))
        .projection(Report)(new ReportStretcher(elastic))
        .projection(Event)(new HomeViewEventsBuilder(viewsRef))
        .projection(Report)(new HomeViewReportsBuilder(viewsRef))
        .projection(Upvote)(new HomeViewUpvotesBuilder(viewsRef))
        .projection(Event)(new EventViewEventsBuilder(viewsRef))
        .projection(Report)(new EventViewReportsBuilder(viewsRef))
        .projection(Upvote)(new EventViewUpvotesBuilder(viewsRef))
        .build
        .run(cancellation)
        .doOnNext
        {
            case Success(Left(result)) => println(s"Command executed: $result")
            case Success(Right(error)) => err.println(s"Command execution error: $error")
            case Success(event)        => println(s"Event processed: $event")
            case Failure(exception)    => err.println(s"Command execution exception: ${exception.fullMessage }")
        }
        .await
    }
}

