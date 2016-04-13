package com.scalasourcing.model

import com.scalasourcing.tools.StringExtensions._
import scala.concurrent.Future

trait Projection[A <: Aggregate]
{
    lazy val name = getClass.getSimpleName.replace("$", "").decapitalize
    def project(id: A#Id, event: A#Event, eventIndex: Int): Future[AnyRef]
    def prepare(): Future[Unit] = Future.successful(())
}
