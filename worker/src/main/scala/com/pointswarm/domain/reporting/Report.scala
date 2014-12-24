package com.pointswarm.domain.reporting

import com.pointswarm.common.dtos.HtmlContent
import com.pointswarm.domain.common.ReportIdAgg
import com.scalasourcing.model._


object Report extends Aggregate
{
    type Id = ReportIdAgg

    case class DoReport(content: HtmlContent) extends Command

    case class Added(originalContent: HtmlContent) extends Event
    case class Edited(newContent: HtmlContent) extends Event

    case object ContentIsTheSame extends Error

    case object NotExistingReport extends State
    {
        def apply(event: Event) = event match
        {
            case Added(content) => ExistingReport(content)
            case _              => this
        }

        def apply(command: Command) = command match
        {
            case DoReport(content) => Added(content)
        }
    }

    case class ExistingReport(content: HtmlContent) extends State
    {
        def apply(event: Event) = event match
        {
            case Edited(newContent) => copy(newContent)
            case _                  => this
        }

        def apply(command: Command) = command match
        {
            case DoReport(newContent) => edit(newContent)
        }

        private def edit(newContent: HtmlContent): Result =
        {
            if (newContent == content) ContentIsTheSame
            else Edited(newContent)
        }
    }

    def seed = NotExistingReport
}