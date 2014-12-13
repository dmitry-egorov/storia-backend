package com.pointswarm.tools.fireLegion

import org.joda.time._

import scala.util._

case class CommandResult(ok: Boolean, data: Option[AnyRef], error: Option[String], createdOn: DateTime)

object CommandResult
{
    def apply(t: Try[AnyRef]) = from(t)

    def from(t: Try[AnyRef]) =
        t match
        {
            case Success(data)  => CommandResult.Ok(data)
            case Failure(cause) => CommandResult.Error(cause)
        }

    def Ok(data: AnyRef) = new CommandResult(true, Some(data), None, DateTime.now(DateTimeZone.UTC))

    def Error(cause: Throwable) = new CommandResult(false, None, Some(cause.getMessage), DateTime.now(DateTimeZone.UTC))
}

