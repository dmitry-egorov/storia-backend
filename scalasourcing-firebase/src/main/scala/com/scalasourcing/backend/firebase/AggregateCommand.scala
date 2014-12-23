package com.scalasourcing.backend.firebase

case class AggregateCommand[Id, Payload](id: Id, payload: Payload)
