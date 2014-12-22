package com.dmitryegorov.futuristic.cancellation

class CancellationSource extends CancellationToken {
    private var cancelled = false
    private var callbacks: List[() => Unit] = List.empty

    def cancel() = {
        cancelled = true
        callbacks.foreach(c => c())
    }

    def whenCancelled(act: () => Unit): Unit = {
        if (cancelled) {
            act()
        }
        else {
            callbacks ::= act
        }
    }

    def isCancelled: Boolean = cancelled
}
