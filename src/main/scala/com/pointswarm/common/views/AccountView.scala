package com.pointswarm.common.views

import com.pointswarm.commands.ProviderType._
import com.pointswarm.common.dtos._

case class AccountView(provider: ProviderType, profileId: ProfileId, providerData: Option[AnyRef])
{
    assert(provider != null)
    assert(profileId != null)
}