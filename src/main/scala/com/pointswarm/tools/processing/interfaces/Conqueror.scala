package com.pointswarm.tools.processing.interfaces

import scala.concurrent.Future

trait Conqueror
{
    def prepare: Future[Unit]

    def conquer: Future[Int]
}
