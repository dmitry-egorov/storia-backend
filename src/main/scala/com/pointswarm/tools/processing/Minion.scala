package com.pointswarm.tools.processing

import scala.concurrent.Future

abstract class Minion[TCommand]
{
    def name: String = this.getClass.getSimpleName

    def execute(command: TCommand): Future[AnyRef]

    def prepare: Future[Unit]
}
