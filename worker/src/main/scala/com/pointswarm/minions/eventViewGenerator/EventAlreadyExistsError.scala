package com.pointswarm.minions.eventViewGenerator

import com.pointswarm.common.dtos._

case class EventAlreadyExistsError(id: EventId) extends RuntimeException(s"Event '$id' already exists")
