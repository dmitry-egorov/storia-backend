package com.pointswarm.tools.fireLegion.distributor

import com.pointswarm.tools.fireLegion._

class MinionsNotFoundException(name: CommandName) extends Exception(s"Unable to find minions for command $name")
