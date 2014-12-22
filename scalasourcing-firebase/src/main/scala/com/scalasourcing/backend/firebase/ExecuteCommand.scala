package com.scalasourcing.backend.firebase

case class ExecuteCommand[Id, Payload](id: Id, payload: Payload)
