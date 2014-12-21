package com.pointswarm.commands

import com.pointswarm.commands.ProviderType._
import com.pointswarm.common.dtos._

case class RegisterCommand(accountId: AccountId, name: Name, provider: ProviderType, providerData: Map[String, AnyRef])
{
    assert(name != null)
    assert(provider != null)
    assert(providerData != null)
}
