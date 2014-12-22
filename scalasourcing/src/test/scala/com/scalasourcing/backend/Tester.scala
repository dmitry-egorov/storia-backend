package com.scalasourcing.backend

import com.scalasourcing.model.{Aggregate, AggregateId}

case class TesterId(value: String) extends AggregateId

object Tester extends Aggregate {
    type Id = TesterId

    case class SomethingHappened() extends Event
    case class DoSomething() extends Command

    case class TesterState() extends State {
        def apply(event: Event) = TesterState()
        def apply(command: Command) = SomethingHappened()
    }

    def seed = TesterState()
}

