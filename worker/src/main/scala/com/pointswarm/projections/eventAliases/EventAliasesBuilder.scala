package com.pointswarm.projections.eventAliases

import com.pointswarm.domain.reporting.Event
import com.pointswarm.domain.reporting.Event._
import com.pointswarm.projections.common.EventAliasStorage
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class EventAliasesBuilder(aliasStorage: EventAliasStorage)(implicit f: Formats, ec: ExecutionContext)
    extends Projection[Event.type]
{
    override def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        val alias = event match
        {
            case Created(t) => t.alias
        }

        aliasStorage.save(id, alias).map(_ => s"Event aliases '$id': created '$alias'")
    }
}
