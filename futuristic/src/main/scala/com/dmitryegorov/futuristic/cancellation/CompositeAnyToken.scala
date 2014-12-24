package com.dmitryegorov.futuristic.cancellation

class CompositeAnyToken(tokens: CancellationToken*) extends CancellationToken
{
    override def whenCancelled(act: () => Unit): Unit = tokens.foreach(x => x.whenCancelled(act))
    override def isCancelled: Boolean = tokens.exists(x => x.isCancelled)
}
