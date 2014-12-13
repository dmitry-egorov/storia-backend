package com.pointswarm.commands

import com.pointswarm.commands.ProviderType._
import com.pointswarm.common.dtos._

case class RegisterCommand(accountId: AccountId, name: String, provider: ProviderType, providerUid: String, providerData: Map[String, AnyRef])
