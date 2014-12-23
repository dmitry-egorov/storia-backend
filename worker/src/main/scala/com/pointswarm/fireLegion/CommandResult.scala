package com.pointswarm.fireLegion

import org.joda.time._

import scala.util._

case class CommandResult[TCommand](ok: Boolean, data: Option[AnyRef], error: Option[String], createdOn: DateTime, command: TCommand)

object CommandResult
{
    def apply[T](t: Try[AnyRef], command: T) = from(t, command)

    def from[T](response: Try[AnyRef], command: T) =
        response match
        {
            case Success(data)  => CommandResult.Ok(data, command)
            case Failure(cause) => CommandResult.Error(cause, command)
        }

    def Ok[T](data: AnyRef, command: T): CommandResult[T] = CommandResult(ok = true, Some(data), None, DateTime
                                                                                                       .now(DateTimeZone
                                                                                                            .UTC), command)

    def Error[T](cause: Throwable, command: T): CommandResult[T] = CommandResult(ok = false, None, Some(cause
                                                                                                        .getMessage), DateTime
                                                                                                                      .now(DateTimeZone
                                                                                                                           .UTC), command)
}

