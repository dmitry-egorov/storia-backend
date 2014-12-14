package com.pointswarm.minions.paparazzi

import com.firebase.client.Firebase
import com.ning.http.client.Response
import com.pointswarm.commands.ProviderType._
import com.pointswarm.common.dtos._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.fireLegion.messenger.SuccessResponse
import com.pointswarm.tools.hellfire.Extensions._
import dispatch._
import org.json4s.DynamicJValue._
import org.json4s.Formats
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods._

import scala.concurrent.{Future, _}

class Paparazzi(root: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Minion[FindSocialPictureCommand]
{
    private lazy val profilesRoot: Firebase = root / "profiles"

    override def execute(commandId: CommandId, command: FindSocialPictureCommand): Future[AnyRef] =
    {
        for
        {
            imageUrl <- findProfileImageUrl(command.provider, command.providerUid)
            a <- setProfileImage(command.profileId, imageUrl)
        } yield SuccessResponse
    }


    private def setProfileImage(profileId: ProfileId, imageUrl: String) =
    {
        profilesRoot / profileId / "image" <-- imageUrl
    }

    private def findProfileImageUrl(provider: ProviderType, providerUid: String): Future[String] =
    {
        provider match
        {
            case Facebook => getFacebookImageUrl(providerUid)
            case Google   => getGoogleImageUrl(providerUid)
            case _        => Future.successful(defaultImageUrl)
        }
    }

    private def getFacebookImageUrl(userId: String): Future[String] =
    {
        Future.successful(s"http://graph.facebook.com/$userId/picture?type=square")
    }

    private def getGoogleImageUrl(userId: String): Future[String] =
    {
        Http(url(s"http://picasaweb.google.com/data/entry/api/user/$userId?alt=json"))
        .map(data => getUrlFromGoogleResponse(data))
        .recover
        {
            case _ => defaultImageUrl
        }
    }

    private def getUrlFromGoogleResponse(data: Response): String =
    {
        val jvalue = dyn(parse(data.getResponseBody)).entry.gphoto$thumbnail.$t.raw

        jvalue match
        {
            case JString(value) => value
            case _              => defaultImageUrl
        }
    }

    private lazy val defaultImageUrl: String =
    {
        "http://pointswarm.com/img/anonymous.png"
    }
}
