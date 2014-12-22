package com.pointswarm.domain.voting

import com.pointswarm.common.dtos.ProfileId
import com.pointswarm.domain.reporting.ReportId
import com.scalasourcing.model._

case class UpvoteId(userId: ProfileId, reportId: ReportId) extends CompositeAggregateId {
    val ids = Seq(userId, reportId)
}

object Upvote extends Aggregate {
    type Id = UpvoteId

    case class Cast() extends Command
    case class Cancel() extends Command

    case class Casted() extends Event
    case class Cancelled() extends Event

    case class WasAlreadyCastedError() extends Error
    case class WasNotCastedError() extends Error

    case class CastedUpvote() extends State {
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

    case class NotCastedUpvote() extends State {
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