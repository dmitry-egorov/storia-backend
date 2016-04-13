package com.pointswarm.commands

import com.pointswarm.common.ProviderType
import ProviderType.ProviderType
import com.pointswarm.common.dtos.ProfileId

case class FindSocialPictureCommand(profileId: ProfileId, provider: ProviderType, providerUid: String)
