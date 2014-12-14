package com.pointswarm.migration

import com.pointswarm.tools.elastic.Client
import com.pointswarm.tools.futuristic.FutureExtensions._

import scala.concurrent._

object Migrator
{
    def createTextIndex(elastic: Client)(implicit e: ExecutionContext): Future[Unit] =
    {
        val index = elastic.index("texts")

        index.exists.flatMap(exists =>
                                 if (!exists)
                                 {
                                     index.create().recoverAsTry.map(_ => ())
                                 }
                                 else
                                 {
                                     Future.successful(())
                                 }
        )
    }
}