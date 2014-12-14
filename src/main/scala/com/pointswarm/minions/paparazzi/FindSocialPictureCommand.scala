package com.pointswarm.minions.paparazzi

import com.pointswarm.commands.ProviderType.ProviderType
import com.pointswarm.common.dtos.ProfileId

case class FindSocialPictureCommand(profileId: ProfileId, provider: ProviderType, providerUid: String)
