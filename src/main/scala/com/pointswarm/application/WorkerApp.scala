package com.pointswarm.application

import com.firebase.client.Firebase
import com.pointswarm.elastic.Client
import com.pointswarm.elasticUpdater.ElasticUpdater
import com.pointswarm.helpers.SystemEx
import com.pointswarm.searcher.Searcher

import scala.util.Properties

object WorkerApp extends App
{

    println(s"Started worker")

    val elasticUrl = Properties.envOrElse("BONSAI_URL", "https://7aj1pw8c:5jx5fndr5kvxwp8o@jasmine-4056315.us-east-1.bonsai.io:443")
    val fbUrl = Properties.envOrElse("FBURL", "https://storia-test.firebaseio.com/")

    var elastic = new Client(elasticUrl)
    var fb = new Firebase(fbUrl)

    var searcherSubs = Searcher.run(fb, elastic)
    var elasticUpdaterSubs = ElasticUpdater.run(fb, elastic)

    SystemEx.waitForShutdown()

    println("Shutting down...")

    searcherSubs.unsubscribe()
    elasticUpdaterSubs.unsubscribe()

    println("Shut down.")
}
