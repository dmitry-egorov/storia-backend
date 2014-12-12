package com.pointswarm.tools.processing.interfaces

import com.pointswarm.tools.futuristic.cancellation.CancellationToken

import scala.concurrent.Future

trait Conqueror
{
    def prepare: Future[Unit]

    def conquer(completeWith: CancellationToken): Future[Int]
}
