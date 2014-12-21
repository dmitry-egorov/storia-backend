package com.pointswarm.domain.voting

import com.pointswarm.common.dtos.ProfileId
import com.pointswarm.domain.reporting.Report
import com.scalasourcing.model._

sealed trait Upvote extends AggregateRoot[Upvote]

object Upvote extends Aggregate[Upvote]
{
    case class Id(userId: ProfileId, reportId: Report.Id) extends Identity with CompositeAggregateId
    {
        val ids = Seq(userId, reportId)
    }
    
    case class Cast() extends Command
    case class Cancel() extends Command

    case class Casted() extends Event
    case class Cancelled() extends Event

    case class WasAlreadyCastedError() extends Error
    case class WasNotCastedError() extends Error

    case class CastedUpvote() extends Upvote
    {
        def apply(event: Event) = event match
        {
            case Cancelled() => NotCastedUpvote()
            case _           => this
        }

        def apply(command: Command) = command match
        {
            case Cast()   => WasAlreadyCastedError()
            case Cancel() => Cancelled()
        }
    }

    case class NotCastedUpvote() extends Upvote
    {
        def apply(event: Event) = event match
        {
            case Casted() => CastedUpvote()
            case _        => this
        }

        def apply(command: Command) = command match
        {
            case Cast()   => Casted()
            case Cancel() => WasNotCastedError()
        }
    }

    def seed = NotCastedUpvote()
}