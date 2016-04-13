package com.pointswarm.domain.reporting

import com.pointswarm.common.dtos.Name
import com.pointswarm.domain.common.EventIdAgg
import com.scalasourcing.model.Aggregate

object Event extends Aggregate
{
    type Id = EventIdAgg

    case class Create(title: Name) extends Command

    case class Created(originalTitle: Name) extends Event

    case object AlreadyExists extends Error

    case object NotExistingEvent extends State
    {
        def apply(event: Event) = event match
        {
            case Created(title) => ExistingEvent(title)
            case _              => this
        }

        def apply(command: Command) = command match
        {
            case Create(title) => Created(title)
        }
    }

    case class ExistingEvent(title: Name) extends State
    {
        def apply(event: Event) = event match
        {
            case _ => this
        }

        def apply(command: Command) = command match
        {
            case Create(newContent) => AlreadyExists
        }
    }

    def seed = NotExistingEvent
}
