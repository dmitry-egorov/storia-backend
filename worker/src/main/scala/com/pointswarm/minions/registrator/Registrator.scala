package com.pointswarm.minions.registrator

import com.dmitryegorov.futuristic.FutureExtensions._
import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client._
import com.pointswarm.commands.ProviderType._
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.fireLegion._
import com.pointswarm.fireLegion.messenger.MessengerExtensions._
import com.pointswarm.fireLegion.messenger.SuccessResponse
import com.pointswarm.minions.paparazzi.FindSocialPictureCommand
import org.json4s._

import scala.concurrent._

class Registrator(root: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[RegisterCommand]
{
    private lazy val accountsRoot = root / "accounts"
    private lazy val profilesRoot: Firebase = root / "profiles"

    def execute(commandId: CommandId, command: RegisterCommand): Future[AnyRef] =
    {
        val accountId = command.accountId
        val name = command.name
        val provider = command.provider
        val providerData = command.providerData
        val providerUid = getProviderUid(providerData)
        val profileId = name.sanitize

        for
        {
            _ <- assertAccountDoesntExist(accountId)
            _ <- setViews(accountId, profileId, name, provider, providerData, providerUid)
        }
        yield SuccessResponse
    }

    private def getProviderUid(providerData: Map[String, AnyRef]) = providerData("id").toString

    private def assertAccountDoesntExist(accountId: AccountId): Future[Unit] =
    {
        accountsRoot / accountId whenExists (() => throw AccountAlreadyExistsException(accountId))
    }

    private def setViews(accountId: AccountId, profileId: ProfileId, name: Name, provider: ProviderType, providerData: AnyRef, providerUid: String): Future[Unit] =
    {
        List(
            setProfileView(name, profileId),
            setAccountView(accountId, profileId, provider, providerData),
            commandPaparazzi(profileId, provider, providerUid)
        ).waitAll
    }

    private def setAccountView(accountId: AccountId, profileId: ProfileId, provider: ProviderType, providerData: AnyRef): Future[String] =
    {
        accountsRoot / accountId <-- AccountView(provider, profileId, Some(providerData))
    }

    private def setProfileView(name: Name, profileId: ProfileId): Future[String] =
    {
        profilesRoot / profileId <-- ProfileView(name, None, "")
    }

    private def commandPaparazzi(profileId: ProfileId, provider: ProviderType, providerUid: String): Future[AnyRef] =
    {
        root request("paparazzi", FindSocialPictureCommand(profileId, provider, providerUid))
    }
}