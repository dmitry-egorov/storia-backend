package com.pointswarm.domain.voting

import com.pointswarm.domain.common.UpvoteIdAgg
import com.scalasourcing.model.Aggregate

object Upvote extends Aggregate
{
    type Id = UpvoteIdAgg

    case object Cast extends Command
    case object Cancel extends Command

    case object Casted extends Event
    case object Cancelled extends Event

    case object WasAlreadyCastedError extends Error
    case object WasNotCastedError extends Error

    case object CastedUpvote extends State
    {
        def apply(event: Event) = event match
        {
            case Cancelled => NotCastedUpvote
            case _         => this
        }

        def apply(command: Command) = command match
        {
            case Cast   => WasAlreadyCastedError
            case Cancel => Cancelled
        }
    }

    case object NotCastedUpvote extends State
    {
        def apply(event: Event) = event match
        {
            case Casted => CastedUpvote
            case _      => this
        }

        def apply(command: Command) = command match
        {
            case Cast   => Casted
            case Cancel => WasNotCastedError
        }
    }

    def seed = NotCastedUpvote
}