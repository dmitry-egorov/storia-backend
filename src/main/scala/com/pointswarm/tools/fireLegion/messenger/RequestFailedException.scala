package com.pointswarm.tools.fireLegion.messenger

import com.pointswarm.tools.fireLegion._

class RequestFailedException[TCommand <: AnyRef](name: MinionName, command: TCommand)(implicit m: Manifest[TCommand])
    extends RuntimeException(s"Request '${CommandName.of[TCommand]}' with payload '$command' to $name failed")
