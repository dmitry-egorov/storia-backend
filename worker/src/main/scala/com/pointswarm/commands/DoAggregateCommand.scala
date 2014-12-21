package com.pointswarm.commands

import com.scalasourcing.model.Aggregate._

case class DoAggregateCommand[Id, Root](id: Id, payload: CommandOf[Root])