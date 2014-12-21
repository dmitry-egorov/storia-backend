package com.pointswarm.fireLegion

import com.dmitryegorov.tools.futuristic.FutureExtensions._
import com.dmitryegorov.tools.futuristic.cancellation._
import com.pointswarm.fireLegion.interfaces.Conqueror

import scala.concurrent._

case class Army(conquerors: List[Conqueror])(implicit ec: ExecutionContext) extends Conqueror
{
    def prepare: Future[Unit] =
    {
        conquerors
        .map(_.prepare)
        .waitAll
    }

    def conquer(completeWith: CancellationToken): Future[Int] =
    {
        conquerors
        .map(_.conquer(completeWith))
        .whenAll
        .map(x => x.sum)
    }
}