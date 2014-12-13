package com.pointswarm.minions.registrator

import com.firebase.client._
import com.pointswarm.commands.ProviderType._
import com.pointswarm.commands._
import com.pointswarm.common.dtos._
import com.pointswarm.common.views._
import com.pointswarm.tools.extensions.SanitizeExtensions._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.hellfire.Extensions._
import com.pointswarm.tools.futuristic.FutureExtensions._
import dispatch._
import org.json4s._

import scala.concurrent.{Future, _}

class Registrator(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[RegisterCommand]
{
    val Future = scala.concurrent.Future

    def execute(commandId: CommandId, command: RegisterCommand): Future[AnyRef] =
    {
        val accountId = command.accountId
        val name = command.name
        val provider = command.provider
        val providerData = command.providerData
        val providerUid = command.providerUid

        assertAccountExists(accountId)
        .flatMap(_ => getProfilePicture(provider, providerUid))
        .flatMap(imageUrl => setViews(accountId, name, provider, providerData, imageUrl))
        .map(_ => SuccessResponse)
    }

    def assertAccountExists(accountId: AccountId): Future[Unit] =
    {
        fb
        .child("accounts")
        .child(accountId)
        .exists
        .map(exists => if (exists) throw new AccountAlreadyExists(accountId))
    }

    def setViews(accountId: AccountId, name: String, provider: ProviderType, providerData: Map[String, AnyRef], imageUrl: String): Future[Unit] =
    {
        val profileId = name.sanitize

        val profileFuture = setProfileView(name, imageUrl, profileId)
        val accountFuture = setAccountView(provider, providerData, accountId, profileId)

        List(profileFuture, accountFuture).waitAll
    }

    def setAccountView(provider: ProviderType, providerData: Map[String, AnyRef], accountId: AccountId, profileId: ProfileId): Future[String] =
    {
        val accountData = new AccountView(provider, profileId, providerData)

        fb
        .child("accounts")
        .child(accountId)
        .set(accountData)
    }

    private def setProfileView(name: String, imageUrl: String, profileId: ProfileId): Future[String] =
    {
        val profileData = new ProfileView(name, imageUrl, "")

        fb
        .child("profiles")
        .child(profileId)
        .set(profileData)
    }

    private def getProfilePicture(provider: ProviderType, userId: String): Future[String] =
    {
        import org.json4s.DynamicJValue._
        import org.json4s.jackson.JsonMethods._

        if (provider == Facebook)
        {
            Future.successful(s"http://graph.facebook.com/$userId/picture?type=square")
        }
        else if (provider == Google)
        {
            Http(url(s"http://picasaweb.google.com/data/entry/api/user/$userId?alt=json"))
            .map(data => dyn(parse(data.getResponseBody)).entry.gphoto$thumbnail.$t.toString)
            .recover
            {
                case _ => "http://pointswarm.com/img/anonymous.png"
            }
        }
        else
        {
            Future.successful("http://pointswarm.com/img/anonymous.png")
        }
    }
}


