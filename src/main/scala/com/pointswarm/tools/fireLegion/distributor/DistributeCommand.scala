package com.pointswarm.tools.fireLegion.distributor

import com.pointswarm.tools.fireLegion._
import org.joda.time._

case class DistributeCommand(name: CommandName, payload: AnyRef, addedOn: DateTime)

object DistributeCommand
{
    def apply[TCommand <: AnyRef](command: TCommand)(implicit m: Manifest[TCommand]): DistributeCommand =
        DistributeCommand(CommandName[TCommand], command, DateTime.now(DateTimeZone.UTC))
}


