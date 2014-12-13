package com.pointswarm.minions.eventViewGenerator

import com.pointswarm.common.dtos._

class EventAlreadyExistsError(id: EventId) extends Exception(s"Event '${id.value }' already exists")
