package com.pointswarm.tools.processing

import scala.concurrent.Future

abstract class Minion[TCommand]
{
    def name: String = this.getClass.getSimpleName

    def obey(command: TCommand): Future[AnyRef]
}
