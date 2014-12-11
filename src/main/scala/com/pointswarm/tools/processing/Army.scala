package com.pointswarm.tools.processing

import com.pointswarm.tools.processing.interfaces.Conqueror
import com.pointswarm.tools.futuristic.FutureExtensions._

import scala.concurrent._

class Army(conquerors: List[Conqueror])(implicit ec: ExecutionContext) extends Conqueror
{
    def prepare: Future[Unit] = conquerors.map(_.prepare).whenAll.map(_ => ())

    def conquer: Future[Int] = Future.sequence(conquerors.map(_.conquer)).map(x => x.sum)
}