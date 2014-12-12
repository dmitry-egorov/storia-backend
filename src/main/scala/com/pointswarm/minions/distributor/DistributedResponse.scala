package com.pointswarm.minions.distributor

import com.pointswarm.tools.processing._
import org.joda.time._

case class DistributedResponse(name: CommandName, payload: AnyRef, addedOn: DateTime, minions: List[MinionName])
