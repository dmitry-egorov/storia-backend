package com.scalasourcing.backend.firebase

import com.firebase.client.Firebase
import com.dmitryegorov.hellfire.Hellfire._
import com.scalasourcing.backend.Tester
import com.scalasourcing.backend.Tester.SomethingHappened
import com.scalasourcing.model.Projection
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

class TesterProjection(fb: Firebase)(implicit f: Formats, ec: ExecutionContext) extends Projection(Tester)
{
    override def consume(id: a.Id, event: a.Event): Future[AnyRef] = event match
    {
        case SomethingHappened => fb / "views" / "tester" / id <-- true
    }

    override def prepare(): Future[Unit] = Future.successful(())
}
