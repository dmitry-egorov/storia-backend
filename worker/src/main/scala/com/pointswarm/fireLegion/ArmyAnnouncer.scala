package com.pointswarm.fireLegion

import com.dmitryegorov.futuristic.cancellation._
import com.pointswarm.fireLegion.interfaces._

import scala.concurrent._
import scala.util._

class ArmyAnnouncer(conqueror: Conqueror)(implicit ec: ExecutionContext) extends Conqueror
{
    override def prepare: Future[Unit] =
    {
        println("Assembling the army...")

        conqueror.prepare
    }

    override def conquer(completeWith: CancellationToken): Future[Int] =
    {
        println("Ready to conquer the world. Awaiting your commands...")

        completeWith.whenCancelled(() => println("Retreating..."))

        conqueror.conquer(completeWith).andThen
        {
            case Success(total) => println(s"Retreated. Total of $total commands executed.")
            case Failure(cause) => println(s"Something went very wrong while retreating, the whole army is dead: $cause")
        }
    }
}

object ArmyAnnouncer
{

    implicit class ArmyEx(val c: Army) extends AnyVal
    {
        def withAnnouncer(implicit ec: ExecutionContext): Conqueror = new ArmyAnnouncer(c)
    }

}
