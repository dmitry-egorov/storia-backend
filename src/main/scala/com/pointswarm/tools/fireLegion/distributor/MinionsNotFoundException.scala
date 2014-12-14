package com.pointswarm.tools.fireLegion.distributor

import com.pointswarm.tools.fireLegion._

case class MinionsNotFoundException(name: CommandName) extends RuntimeException(s"Unable to find minions for command $name")
