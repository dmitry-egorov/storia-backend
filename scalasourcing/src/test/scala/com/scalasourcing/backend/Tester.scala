package com.scalasourcing.backend

import com.scalasourcing.model.Aggregate
import com.scalasourcing.model.id.{AggregateId, CompositeAggregateId, StringAggregateId}

case class SubId1(value: String) extends StringAggregateId
case class SubId2(value: String) extends StringAggregateId
case class TesterId(subId1: SubId1, subId2: SubId2) extends CompositeAggregateId
{
    override def ids: Seq[AggregateId] = Seq(subId1, subId2)
}

object Tester extends Aggregate
{
    type Id = TesterId

    case object SomethingHappened extends Event
    case object DoSomething extends Command

    case object SomeState extends State
    {
        def apply(event: Event) = SomeState
        def apply(command: Command) = SomethingHappened
    }

    def seed = SomeState
}

