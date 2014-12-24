package com.scalasourcing.backend.firebase

import com.scalasourcing.model.Aggregate

case class CommandResult[A <: Aggregate](command: A#Command, result: A#Result)
