package com.scalasourcing.backend.firebase

import com.scalasourcing.model.Aggregate.CommandOf

case class ExecuteCommand[Id, Root](id: Id, payload: CommandOf[Root])
