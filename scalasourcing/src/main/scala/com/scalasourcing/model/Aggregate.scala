package com.scalasourcing.model

import com.scalasourcing.model.id.AggregateId
import com.scalasourcing.tools.StringExtensions._

trait Aggregate
{
    def seed: State

    def name = getClass.getSimpleName.replace("$", "").decapitalize

    type Id <: AggregateId
    trait Command extends AggregateCommand
    trait Event extends AggregateEvent
    trait Error extends AggregateError
    type EventsSeq = Seq[Event]
    type Result = Either[EventsSeq, Error]

    implicit protected def ok(event: Event): Result = Left(Seq(event))
    implicit protected def error(error: Error): Result = Right(error)

    trait State
    {
        def apply(event: Event): State
        def apply(command: Command): Result

        def append(event: Event): State = apply(event)
        def append(events: EventsSeq): State = events.foldLeft(this)((ar, e) => ar + e)
        def append(result: Result): State = result.fold(events => append(events), error => this)
        def execute(command: Command): Result = apply(command)
        def appendResultOf(command: Command): State = append(execute(command))

        def +(event: Event) = append(event)
        def +(events: EventsSeq) = append(events)
        def +(result: Result) = append(result)
        def !(command: Command) = execute(command)
        def +!(command: Command) = appendResultOf(command)
    }
}

object Aggregate
{
    type AggregateEventsSeq = Seq[AggregateEvent]
    type AggregateResult = Either[AggregateEventsSeq, AggregateError]
}
