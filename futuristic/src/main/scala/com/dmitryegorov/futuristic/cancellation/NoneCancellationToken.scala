package com.dmitryegorov.futuristic.cancellation

object NoneCancellationToken extends CancellationToken
{
    def whenCancelled(act: () => Unit): Unit = {}

    val isCancelled: Boolean = false
}
