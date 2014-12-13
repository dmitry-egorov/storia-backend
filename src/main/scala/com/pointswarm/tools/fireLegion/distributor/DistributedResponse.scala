package com.pointswarm.tools.fireLegion.distributor

import com.pointswarm.tools.fireLegion._
import org.joda.time._

case class DistributedResponse(name: CommandName, payload: AnyRef, addedOn: DateTime, minions: List[MinionName])
