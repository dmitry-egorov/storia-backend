package com.pointswarm.migration

import com.pointswarm.elastic.Client

import scala.concurrent.Future

object Migrator
{
    def migrate(elastic: Client): Future[Unit] =
    {
        elastic.index("texts", "text").create()
    }
}
