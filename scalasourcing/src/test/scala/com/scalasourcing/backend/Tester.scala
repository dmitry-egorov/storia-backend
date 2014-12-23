package com.scalasourcing.backend

import com.scalasourcing.model.{Aggregate, AggregateId}

case class TesterId(value: String) extends AggregateId

object Tester extends Aggregate
{
    type Id = TesterId

    case object SomethingHappened extends Event
    case object DoSomething extends Command

    case object TesterState extends State
    {
        def apply(event: Event) = TesterState
        def apply(command: Command) = SomethingHappened
    }

    def seed = TesterState
}

