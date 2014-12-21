package com.scalasourcing.backend

import com.scalasourcing.model.Aggregate.AggregateId
import com.scalasourcing.model._

trait TestRoot extends AggregateRoot[TestRoot]

case class TestRootId(value: String) extends AggregateId

object TestRoot extends Aggregate[TestRoot]
{
    type Id = TestRootId

    case class RootEvent() extends Event
    case class RootCommand() extends Command

    case class SimpleTestRoot() extends TestRoot
    {
        def apply(event: Event) = SimpleTestRoot()
        def apply(command: Command) = RootEvent()
    }

    def seed = SimpleTestRoot()
}


