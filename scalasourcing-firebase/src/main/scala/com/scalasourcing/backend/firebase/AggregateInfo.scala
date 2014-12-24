package com.scalasourcing.backend.firebase

import com.scalasourcing.model.id.AggregateId

case class AggregateInfo[Id <: AggregateId](id: Id, version: Int)
