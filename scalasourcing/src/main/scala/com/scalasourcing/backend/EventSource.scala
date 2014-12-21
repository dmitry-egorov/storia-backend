package com.scalasourcing.backend

import com.scalasourcing.model.Aggregate.EventOf

trait EventSource
{
    def subscribe[AR: Manifest](f: EventOf[AR] => Unit): () => Unit
}
