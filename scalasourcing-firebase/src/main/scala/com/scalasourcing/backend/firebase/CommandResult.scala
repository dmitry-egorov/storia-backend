package com.scalasourcing.backend.firebase

import com.scalasourcing.model.Aggregate
import org.joda.time.DateTime

case class CommandResult[A <: Aggregate](command: A#Command, result: A#Result, addedOn: DateTime, processedOn: DateTime)
