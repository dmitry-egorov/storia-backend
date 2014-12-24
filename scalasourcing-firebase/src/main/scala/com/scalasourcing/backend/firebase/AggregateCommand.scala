package com.scalasourcing.backend.firebase

import org.joda.time.DateTime

case class AggregateCommand[Id, Payload](id: Id, payload: Payload, addedOn: DateTime)
