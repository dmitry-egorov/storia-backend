package com.scalasourcing.backend.firebase.domain

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.scalasourcing.backend.Tester
import com.scalasourcing.backend.Tester.SomethingHappened
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class TesterProjection(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Projection[Tester.type]
{
    override def project(id: Tester.Id, event: Tester.Event, eventIndex: Int): Future[AnyRef] = event match
    {
        case SomethingHappened => fb / "views" / "tester" / id.hash <-- true
    }
}
