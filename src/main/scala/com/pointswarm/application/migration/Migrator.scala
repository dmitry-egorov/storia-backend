package com.pointswarm.application.migration

import com.pointswarm.tools.elastic.Client
import com.pointswarm.tools.futuristic.FutureExtensions._

import scala.async.Async._
import scala.concurrent._

object Migrator
{
    def createTextIndex(elastic: Client)(implicit e: ExecutionContext): Future[Unit] =
        async
        {
            val index = elastic.index("texts")
            val exists = await(index exists)
            if (!exists)
            {
                await(index.create().recoverAsTry)
            }
        }
}
