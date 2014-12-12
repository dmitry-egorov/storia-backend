package com.pointswarm.commands

import com.pointswarm.tools.processing._
import org.joda.time._

case class DistributeCommand(name: CommandName, payload: AnyRef, addedOn: DateTime)

