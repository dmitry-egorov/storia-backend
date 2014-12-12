package com.pointswarm.tools.futuristic.cancellation

import scala.concurrent.{Promise, Future}

object CancellationToken
{
    def none: CancellationToken = new NoneCancellationToken
}

trait CancellationToken
{
    def whenCancelled(act: () => Unit)

    def isCancelled: Boolean

    def asFuture: Future[Unit] =
    {
        val p = Promise[Unit]()

        whenCancelled(() => p.success(()))

        p.future
    }
}
