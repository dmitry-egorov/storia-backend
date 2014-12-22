package com.pointswarm.fireLegion

import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.pointswarm.fireLegion.interfaces.Conqueror

import scala.concurrent._

abstract class Minion[TCommand <: AnyRef](implicit ec: ExecutionContext) extends Conqueror {
    def execute(commandId: CommandId, command: TCommand): Future[AnyRef]

    def prepare: Future[Unit] = Future.successful(())

    def conquer(completeWith: CancellationToken): Future[Int] = completeWith.asFuture.map(_ => 0)
}
