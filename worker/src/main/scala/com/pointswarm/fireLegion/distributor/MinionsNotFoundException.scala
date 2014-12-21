package com.pointswarm.fireLegion.distributor

import com.pointswarm.fireLegion._

case class MinionsNotFoundException(name: CommandName) extends RuntimeException(s"Unable to find minions for command $name")
