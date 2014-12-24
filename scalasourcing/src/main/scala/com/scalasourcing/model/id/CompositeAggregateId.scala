package com.scalasourcing.model.id

import com.scalasourcing.tools.StringExtensions._

trait CompositeAggregateId extends AggregateId
{
    def ids: Seq[AggregateId]
    lazy val hash = ids.map(_.hash).mkString("+").hash
}
