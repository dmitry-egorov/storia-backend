package com.pointswarm.fireLegion.messenger

import com.firebase.client._
import com.pointswarm.fireLegion._
import com.pointswarm.fireLegion.distributor._
import com.dmitryegorov.hellfire.Hellfire._
import org.json4s._

import scala.concurrent._
import scala.concurrent.duration.Duration

object MessengerExtensions
{

    implicit class FirebaseMessengerEx(val root: Firebase)(implicit f: Formats, ec: ExecutionContext)
    {
        private lazy val minionsRoot: Firebase = root / "minions"

        def request[TCommand <: AnyRef](name: MinionName, command: TCommand, timeout: Duration = Duration.Inf)(implicit m: Manifest[TCommand])
                                       : Future[Option[AnyRef]] =
        {
            for
            {
                key <- sendMessage(command)
                result <- awaitResult(name, command, timeout, key)
            }
            yield result
        }

        private def sendMessage[TCommand <: AnyRef](command: TCommand)(implicit m: Manifest[TCommand]): Future[String] =
        {
            minionsRoot / "distributor" / "inbox" <%- DistributeCommand(command)
        }

        private def awaitResult[TCommand <: AnyRef](name: MinionName, command: TCommand, timeout: Duration, key: String)(implicit m: Manifest[TCommand]): Future[Option[AnyRef]] =
        {
            (minionsRoot / name / "results" / key)
            .awaitValue[CommandResult[TCommand]](timeout)
            .map(value => if (!value.ok) throw RequestFailedException(name, command) else value.data)
        }
    }

}

