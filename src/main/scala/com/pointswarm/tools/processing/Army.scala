package com.pointswarm.tools.processing

import com.pointswarm.tools.futuristic.FutureExtensions._
import com.pointswarm.tools.futuristic.cancellation._
import com.pointswarm.tools.processing.interfaces.Conqueror

import scala.concurrent._

class Army(conquerors: List[Conqueror])(implicit ec: ExecutionContext) extends Conqueror
{
    def prepare: Future[Unit] =
    {
        conquerors
        .map(_.prepare)
        .whenAll
        .map(_ => ())
    }

    def conquer(completeWith: CancellationToken): Future[Int] =
    {
        conquerors
        .map(_.conquer(completeWith))
        .whenAll
        .map(x => x.sum)
    }
}