package com.pointswarm.tools.futuristic

import scala.concurrent._
import scala.concurrent.duration._

object Futuristic
{
    private val timer = new java.util.Timer()

    def timeoutFail[T](duration: Duration)(implicit ec: ExecutionContext): Future[T] =
    {
        val p = Promise[T]()

        if (duration != Duration.Inf)
        {
            timer.schedule(new java.util.TimerTask
            {
                def run()
                {
                    p.failure(new TimeoutException)
                }
            }, duration.toMillis)
        }

        p.future
    }

    def timeout[T](value: T, duration: Duration)(implicit ec: ExecutionContext): Future[T] =
    {
        val p = Promise[T]()
        if (duration != Duration.Inf)
        {
            timer.schedule(new java.util.TimerTask
            {
                def run()
                {
                    p.success(value)
                }
            }, duration.toMillis)
        }

        p.future
    }
}
