package com.pointswarm.domain.reporting

import com.pointswarm.common.dtos.{EventId, ProfileId, HtmlContent}
import com.scalasourcing.model._

sealed trait Report extends AggregateRoot[Report]

object Report extends Aggregate[Report]
{
    case class Id(userId: ProfileId, eventId: EventId) extends Identity with CompositeAggregateId
    {
        val ids = Seq(userId, eventId)
    }

    case class DoReport(content: HtmlContent) extends Command

    case class Created(originalContent: HtmlContent) extends Event
    case class Edited(newContent: HtmlContent) extends Event

    case class ContentIsTheSame() extends Error

    case class NotExistingReport() extends Report
    {
        def apply(event: Event) = event match
        {
            case Created(content) => ExistingReport(content)
            case _ => this
        }

        def apply(command: Command) = command match
        {
            case DoReport(content) => Created(content)
        }
    }

    case class ExistingReport(content: HtmlContent) extends Report
    {
        def apply(event: Event) = event match
        {
            case Edited(newContent) => copy(newContent)
            case _ => this
        }

        def apply(command: Command) = command match
        {
            case DoReport(newContent) => edit(newContent)
        }

        private def edit(newContent: HtmlContent): CommandResult =
        {
            if(newContent == content) ContentIsTheSame()
            else Edited(newContent)
        }
    }

    def seed: Report = NotExistingReport()

}