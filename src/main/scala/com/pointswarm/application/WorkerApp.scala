package com.pointswarm.application

import com.firebase.client.Firebase
import com.pointswarm.tools.elastic.Client
import com.pointswarm.elasticUpdater.{AddReportElasticUpdater, AddEventElasticUpdater}
import com.pointswarm.tools.helpers.SystemEx
import com.pointswarm.application.migration.Migrator
import com.pointswarm.searcher.Searcher

import scala.concurrent.Await
import scala.concurrent.duration._

object WorkerApp extends App
{
    println(s"Started worker")

    var elastic = new Client(WorkerConfig.elasticUrl)

    Await.result(Migrator.migrate(elastic), 10 seconds)

    var fb = new Firebase(WorkerConfig.fbUrl)

    var searcherSubs = Searcher.run(fb, elastic)
    var addEventElasticUpdaterSubs = AddEventElasticUpdater.run(fb, elastic)
    var addReportElasticUpdaterSubs = AddReportElasticUpdater.run(fb, elastic)

    SystemEx.waitForShutdown()

    println("Shutting down...")

    searcherSubs.unsubscribe()
    addEventElasticUpdaterSubs.unsubscribe()
    addReportElasticUpdaterSubs.unsubscribe()

    println("Shut down.")
}




