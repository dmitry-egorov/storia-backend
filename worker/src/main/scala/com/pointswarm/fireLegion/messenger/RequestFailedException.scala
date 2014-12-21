package com.pointswarm.fireLegion.messenger

import com.pointswarm.fireLegion._

case class RequestFailedException[TCommand <: AnyRef : Manifest](name: MinionName, command: TCommand)
    extends RuntimeException(s"Request '${CommandName[TCommand]}' with payload '$command' to $name failed")
