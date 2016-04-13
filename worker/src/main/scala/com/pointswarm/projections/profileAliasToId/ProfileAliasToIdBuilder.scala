package com.pointswarm.projections.profileAliasToId

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.Name
import com.pointswarm.domain.profiling.Profile
import com.pointswarm.domain.profiling.Profile._
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class ProfileAliasToIdBuilder(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Projection[Profile.type]
{
    private lazy val profileAliasToIdRef = fb / "profileAliasToId"

    def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        event match
        {
            case Created(accountId, _, data) =>
                val alias = Name(data("displayName").toString).alias

                for
                {
                    _ <- profileAliasToIdRef / alias <-- id
                }
                yield s"Profile: set name of '$id' to '$name'"

        }
    }
}
