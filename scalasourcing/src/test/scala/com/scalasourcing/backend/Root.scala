package com.scalasourcing.backend

import com.scalasourcing.model._

trait Root extends AggregateRoot[Root]

object Root extends Aggregate[Root]
{
    case class Id(value: String) extends Identity

    case class RootEvent() extends Event
    case class RootCommand() extends Command

    case class SimpleRoot() extends Root
    {
        def apply(event: Event) = SimpleRoot()
        def apply(command: Command) = RootEvent()
    }

    def seed = SimpleRoot()
}


