package com.pointswarm.tools.processing

import org.joda.time._

import scala.util._

object Result
{
    def apply(t: Try[AnyRef]) = from(t)

    def from(t: Try[AnyRef]) =
        t match
        {
            case Success(data)  => Result.Ok(data)
            case Failure(cause) => Result.Error(cause)
        }

    def Ok(data: AnyRef) = new Result(true, Some(data), None, DateTime.now(DateTimeZone.UTC))

    def Error(cause: Throwable) = new Result(false, None, Some(cause.getMessage), DateTime.now(DateTimeZone.UTC))
}

case class Result(ok: Boolean, data: Option[AnyRef], error: Option[String], createdOn: DateTime)
