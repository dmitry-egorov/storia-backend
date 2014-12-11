package com.pointswarm.tools.futuristic.cancellation

object CancellationToken
{
    def none: CancellationToken = new NoneCancellationToken
}

trait CancellationToken
{
    def whenCancelled(act: () => Unit)

    def isCancelled: Boolean
}
