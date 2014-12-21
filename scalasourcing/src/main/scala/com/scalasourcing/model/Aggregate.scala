package com.scalasourcing.model

import com.scalasourcing.model.Aggregate._

trait Aggregate[Root <: AggregateRoot[Root]] extends Factory[Root]
{
    implicit val factory: Factory[Root] = this

    type Identity = IdOf[Root]
    type Command = CommandOf[Root]
    type Event = EventOf[Root]
    type Error = ErrorOf[Root]
    type EventsSeq = EventsSeqOf[Root]
    type CommandResult = CommandResultOf[Root]

    implicit def toRichEventsSeq(events: EventsSeq): RichEventsSeqOf[Root] = new RichEventsSeqOf(events)

    implicit protected def ok(event: Event): CommandResult = Left(Seq(event))
    implicit protected def error(error: Error): CommandResult = Right(error)
}

object Aggregate
{
    trait AggregateCommand
    trait AggregateEvent
    trait AggregateError
    trait AggregateId
    {
        def value: String
        override def toString = value
    }
    type AggregateCommandResult = Either[Seq[AggregateEvent], AggregateError]

    trait IdOf[Root] extends AggregateId
    trait CommandOf[Root] extends AggregateCommand
    trait EventOf[Root] extends AggregateEvent
    trait ErrorOf[Root] extends AggregateError

    type EventsSeqOf[Root] = Seq[EventOf[Root]]
    type CommandResultOf[Root] = Either[EventsSeqOf[Root], ErrorOf[Root]]
    case class StateAndResultOf[Root](state: Root, result: CommandResultOf[Root])

    trait Factory[Root]
    {
        def seed: Root
    }

    implicit class RichEventsSeqOf[Root <: AggregateRoot[Root]](val events: EventsSeqOf[Root]) extends AnyVal
    {
        def mkRoot()(implicit f: Factory[Root]): Root = events.foldLeft(f.seed)((r, e) => r(e))

        def !(command: CommandOf[Root])(implicit f: Factory[Root]): CommandResultOf[Root] = mkRoot ! command
    }
}
