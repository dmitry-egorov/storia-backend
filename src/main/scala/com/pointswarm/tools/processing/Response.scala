package com.pointswarm.tools.processing

import org.joda.time.{DateTime, DateTimeZone}

import scala.util.{Failure, Success, Try}

object Response
{

    def apply(t: Try[AnyRef]) = from(t)

    def from(t: Try[AnyRef]) =
        t match
        {
            case Success(data)  => Response.Ok(data)
            case Failure(cause) => Response.Error(cause)
        }

    def Ok(data: AnyRef) = new Response(true, Some(data), None, DateTime.now(DateTimeZone.UTC))

    def Error(cause: Throwable) = new Response(false, None, Some(cause.getMessage), DateTime.now(DateTimeZone.UTC))
}

case class Response(ok: Boolean, data: Option[AnyRef], error: Option[String], createdOn: DateTime)
