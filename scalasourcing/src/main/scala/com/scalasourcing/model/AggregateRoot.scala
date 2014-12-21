package com.scalasourcing.model

import com.scalasourcing.model.Aggregate._

trait AggregateRoot[Root <: AggregateRoot[Root]]
{
    self: Root =>

    def apply(event: EventOf[Root]): Root
    def apply(command: CommandOf[Root]): CommandResultOf[Root]

    def apply(events: EventsSeqOf[Root]): Root = events.foldLeft(self)((ar, e) => ar(e))
    def apply(result: CommandResultOf[Root]): Root = result.fold(events => self(events), error => self)
    def appendResultOf(command: CommandOf[Root]): Root = self(self ! command)
    def stateAndResultOf(command: CommandOf[Root]): StateAndResultOf[Root] =
    {
        val result = self(command)
        StateAndResultOf(self(result), result)
    }

    def append(event: EventOf[Root]): Root = self(event)
    def append(events: EventsSeqOf[Root]): Root = self(events)
    def execute(command: CommandOf[Root]): CommandResultOf[Root] = self(command)

    def +(event: EventOf[Root]) = self(event)
    def +(events: EventsSeqOf[Root]) = self(events)
    def +(result: CommandResultOf[Root]) = self(result)
    def !(command: CommandOf[Root]) = self(command)
    def +!(command: CommandOf[Root]) = appendResultOf(command)
    def +!!(command: CommandOf[Root]) = stateAndResultOf(command)
}

