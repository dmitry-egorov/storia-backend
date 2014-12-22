package com.scalasourcing.bdd

import com.scalasourcing.model.Aggregate._
import com.scalasourcing.model._

trait AggregateBDD[R <: AggregateRoot[R]]
{
    implicit val agg : Aggregate[R]

    def given: EmptyFlowGiven = EmptyFlowGiven()
    def given_nothing: FlowGiven = FlowGiven(agg.seed)

    case class EmptyFlowGiven()
    {
        def it_was(events: agg.Event*): FlowGiven = FlowGiven(events mkRoot)
        def nothing = FlowGiven(agg.seed)
    }

    case class FlowGiven(state: R)
    {
        def and(events: agg.Event*): FlowGiven = FlowGiven(state + events)
        def when_I(command: agg.Command): FlowWhen = FlowWhen(state ! command)
    }

    case class FlowWhen(eventsTry: agg.Result)
    {
        def then_it_is(expected: agg.Event*) =
        {
            eventsTry.fold(
                events => assert(events == expected, s"Invalid events produced. Expected: $expected. Actual: $events"),
                error => assert(assertion = false, s"Expected events: $expected, but was error: $error")
            )
        }

        def then_expect(expected: agg.Error): Unit =
        {
            eventsTry.fold(
                events => assert(assertion = false, s"Expected error $expected, but was events: $events"),
                error => assert(error == expected, s"Invalid error produced. Expected: $expected. Actual: $error")
            )
        }
    }
}