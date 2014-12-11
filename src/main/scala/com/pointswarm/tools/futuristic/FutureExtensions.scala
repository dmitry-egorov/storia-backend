package com.pointswarm.tools.futuristic


import scala.concurrent._
import scala.util._

object FutureExtensions
{

    implicit class FutureTryEx[T](future: Future[Try[T]])
    {
        def flatRecoverAsTry(implicit ec: ExecutionContext): Future[Try[T]] = future.recoverAsTry.map(_.flatten)
    }

    implicit class FutureEx[T](future: Future[T])
    {

        def recoverAsTry(implicit ec: ExecutionContext): Future[Try[T]] =
            future
            .map
            {
                case response => new Success(response)
            }
            .recover
            {
                case cause => new Failure(cause)
            }
    }

}


