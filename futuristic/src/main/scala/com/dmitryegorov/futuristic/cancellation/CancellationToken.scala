package com.dmitryegorov.futuristic.cancellation

import scala.concurrent._

object CancellationToken {
    def none: CancellationToken = NoneCancellationToken
}

trait CancellationToken {
    def whenCancelled(act: () => Unit)

    def isCancelled: Boolean

    def asFuture: Future[Unit] = {
        val p = Promise[Unit]()

        whenCancelled(() => p.success(()))

        p.future
    }
}
