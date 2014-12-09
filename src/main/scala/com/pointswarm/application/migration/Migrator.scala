package com.pointswarm.application.migration

import com.pointswarm.tools.elastic.Client

import scala.concurrent.Future

object Migrator
{
    def migrate(elastic: Client): Future[Unit] =
    {
        elastic.index("texts", "text").create()
    }
}
