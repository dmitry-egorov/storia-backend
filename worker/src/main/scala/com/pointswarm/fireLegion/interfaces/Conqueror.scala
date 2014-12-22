package com.pointswarm.fireLegion.interfaces

import com.dmitryegorov.futuristic.cancellation.CancellationToken

import scala.concurrent.Future

trait Conqueror {
    def prepare: Future[Unit]

    def conquer(completeWith: CancellationToken): Future[Int]
}
