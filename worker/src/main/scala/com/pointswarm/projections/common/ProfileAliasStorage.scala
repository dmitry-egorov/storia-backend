package com.pointswarm.projections.common

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.Alias
import com.pointswarm.domain.common.ProfileIdAgg
import org.json4s.Formats

import scala.concurrent._
import scala.concurrent.duration._

class ProfileAliasStorage(fb: Firebase)(implicit f: Formats, ec: ExecutionContext)
{
    private lazy val profilesRef: Firebase = fb / "profiles"
    private def aliasRefOf(id: ProfileIdAgg): Firebase = profilesRef / id / "alias"

    def getAliasOf(id: ProfileIdAgg): Future[Alias] =
    {
        aliasRefOf(id).awaitValue[Alias](10 seconds)
    }

    def save(id: ProfileIdAgg, alias: Alias): Future[Unit] =
    {
        (aliasRefOf(id) <-- alias).map(_ => ())
    }
}
