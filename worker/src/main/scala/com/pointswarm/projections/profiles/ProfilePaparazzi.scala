package com.pointswarm.projections.profiles

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.ning.http.client.Response
import com.pointswarm.common.ProviderType
import ProviderType._
import com.pointswarm.domain.common.ProfileIdAgg
import com.pointswarm.domain.profiling.Profile
import com.pointswarm.domain.profiling.Profile._
import com.scalasourcing.model.Projection
import dispatch._
import org.json4s.DynamicJValue._
import org.json4s.Formats
import org.json4s.JsonAST.JString
import org.json4s.jackson.JsonMethods._

import scala.concurrent.{ExecutionContext, Future}

class ProfilePaparazzi(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Projection[Profile.type]
{
    private lazy val profilesRoot: Firebase = fb / "profiles"

    def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        event match
        {
            case Created(provider, data) =>
                for
                {
                    imageUrl <- findProfileImageUrl(provider, getProviderUid(data))
                    _ <- setProfileImage(id, imageUrl)
                }
                yield s"Profile: updated image of '$id' to '$imageUrl'"
        }

    }

    private def setProfileImage(id: ProfileIdAgg, imageUrl: String) = profilesRoot / id / "image" <-- imageUrl

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

    private def getProviderUid(data: Map[String, AnyRef]) = data("id").toString
}
