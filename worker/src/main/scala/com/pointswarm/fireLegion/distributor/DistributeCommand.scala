package com.pointswarm.fireLegion.distributor

import com.pointswarm.fireLegion._
import org.joda.time._

case class DistributeCommand(name: CommandName, payload: AnyRef, addedOn: DateTime)

object DistributeCommand {
    def apply[TCommand <: AnyRef : Manifest](command: TCommand): DistributeCommand =
        DistributeCommand(CommandName[TCommand], command, DateTime.now(DateTimeZone.UTC))
}


