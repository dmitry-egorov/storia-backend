package com.pointswarm.tools.futuristic.cancellation

class NoneCancellationToken extends CancellationToken
{
    def whenCancelled(act: () => Unit): Unit = {}

    val isCancelled: Boolean = false
}
