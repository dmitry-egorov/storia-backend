package com.pointswarm.projections.common

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.Name
import com.pointswarm.domain.common.ProfileIdAgg
import org.json4s.Formats

import scala.concurrent._
import scala.concurrent.duration._

class AuthorViewLoader(fb: Firebase)(implicit f: Formats, ec: ExecutionContext)
{
    private lazy val profilesRef: Firebase = fb / "profiles"
    private def profileRef(id: ProfileIdAgg): Firebase = profilesRef / id

    def loadAuthor(id: ProfileIdAgg): Future[AuthorView] =
    {
        val f1 = (profileRef(id) / "name").awaitValue[Name](10 seconds)
        val f2 = (profileRef(id) / "image").awaitValue[String](10 seconds)

        for
        {
            name <- f1
            image <- f2
        }
        yield new AuthorView(name, image, "")
    }
}
