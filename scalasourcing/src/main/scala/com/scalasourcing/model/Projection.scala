package com.scalasourcing.model

import com.scalasourcing.tools.StringExtensions._
import scala.concurrent.Future

abstract class Projection[A <: Aggregate](val a: A)
{
    def name = getClass.getSimpleName.replace("$", "").decapitalize
    def consume(id: a.Id, event: a.Event): Future[AnyRef]
    def prepare(): Future[Unit]
}
