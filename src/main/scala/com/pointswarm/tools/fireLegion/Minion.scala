package com.pointswarm.tools.fireLegion

import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.fireLegion.interfaces.Conqueror

import scala.concurrent._

abstract class Minion[TCommand <: AnyRef](implicit ec: ExecutionContext) extends Conqueror
{
    def execute(commandId: CommandId, command: TCommand): Future[AnyRef]

    def prepare: Future[Unit] = Future.successful(())

    def conquer(completeWith: CancellationToken): Future[Int] = completeWith.asFuture.map(_ => 0)
}