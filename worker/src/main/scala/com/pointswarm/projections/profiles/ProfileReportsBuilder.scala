package com.pointswarm.projections.profiles

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.domain.reporting.Report
import com.pointswarm.domain.reporting.Report._
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class ProfileReportsBuilder(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Projection[Report.type]
{
    private lazy val profileRef = fb / "profiles"

    def project(id: Id, event: Event, eventIndex: Int): Future[AnyRef] =
    {
        event match
        {
            case Added(_) =>
                for
                {
                    _ <- profileRef / id.authorId / "reportedOn" / id.eventId <-- true
                }
                yield s"Profile: user '${id.authorId }' reported on '${id.eventId }'"

            case Edited(_) => Future.successful("Profile: nothing to update")
        }
    }
}
