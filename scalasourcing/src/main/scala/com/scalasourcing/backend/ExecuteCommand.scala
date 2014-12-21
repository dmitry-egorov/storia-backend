package com.scalasourcing.backend

import com.scalasourcing.model.Aggregate.CommandOf

case class ExecuteCommand[Id, Root](id: Id, payload: CommandOf[Root])
