package com.pointswarm.tools.processing

import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import com.pointswarm.tools.processing.interfaces.Conqueror

import scala.concurrent._
import scala.async.Async._

abstract class Minion[TCommand <: AnyRef](implicit ec: ExecutionContext) extends Conqueror
{
    def execute(commandId: CommandId, command: TCommand): Future[AnyRef]

    def prepare: Future[Unit] = async {}

    def conquer(completeWith: CancellationToken): Future[Int] = completeWith.asFuture.map(_ => 0)
}
