package com.pointswarm.commands

import com.scalasourcing.model.Aggregate._

case class DoAggregateCommand(id: AggregateId, payload: AggregateCommand)