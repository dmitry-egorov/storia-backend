package com.pointswarm.projections.profiles

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.Name
import com.pointswarm.domain.profiling.Profile
import com.pointswarm.domain.profiling.Profile._
import com.pointswarm.projections.common.ProfileAliasStorage
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class ProfileInfoBuilder(fb: Firebase, profileAliasStorage: ProfileAliasStorage)(implicit f: Formats, ec: ExecutionContext) extends Projection[Profile.type]
{
    private lazy val profileRef = fb / "profiles"

    def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        event match
        {
            case Created(accountId, _, data) =>
                val name = Name(data("displayName").toString)

                val f1 = profileRef / id / "name" <-- name
                val f2 = profileRef / id / "accountId" <-- accountId
                val f3 = profileAliasStorage.save(id, name.alias)
                for
                {
                    _ <- f1
                    _ <- f2
                    _ <- f3
                }
                yield s"Profile: set name of '$id' to '$name'"

        }
    }
}
