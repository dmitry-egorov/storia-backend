package com.scalasourcing.backend

import com.scalasourcing.model.Aggregate
import org.joda.time.DateTime

trait ExecutionResult
case class CommandExecutionResult[A <: Aggregate](id: A#Id, command: A#Command, result: A#Result, addedOn: DateTime, processedOn: DateTime) extends ExecutionResult
case class ProjectionExecutionResult[A <: Aggregate](id: A#Id, event: A#Event, name: String, result: AnyRef) extends ExecutionResult
