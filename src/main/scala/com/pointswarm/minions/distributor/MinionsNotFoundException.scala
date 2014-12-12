package com.pointswarm.minions.distributor

import com.pointswarm.common._
import com.pointswarm.tools.processing._

class MinionsNotFoundException(name: CommandName) extends Exception(s"Unable to find minions for command $name")
