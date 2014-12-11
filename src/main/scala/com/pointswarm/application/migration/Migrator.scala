package com.pointswarm.application.migration

import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.elastic.Client

import scala.concurrent._

object Migrator
{
    def migrate(elastic: Client)(implicit e: ExecutionContext): Future[Unit] =
    {
        elastic.index("texts", "text").create().recoverAsTry.map(_ => ())
    }
}
