package com.pointswarm.tools.fireLegion.messenger

import com.firebase.client._
import com.pointswarm.tools.fireLegion._
import com.pointswarm.tools.fireLegion.distributor._
import com.pointswarm.tools.hellfire.Extensions._
import org.json4s._

import scala.concurrent._

object MessengerExtensions
{
    implicit class FirebaseMessengerEx(val fb: Firebase)
    {
        def request[TCommand <: AnyRef](name: MinionName, command: TCommand)(implicit m: Manifest[TCommand], f: Formats, ec: ExecutionContext): Future[Option[AnyRef]] =
        {
            //NOTE: going through distributor, since it logs messages
            val commandRef =
                fb
                .child("minions")
                .child("distributor")
                .child("inbox")

            commandRef
            .push(DistributeCommand.of(command))
            .flatMap(key =>
                         fb
                         .child("minions")
                         .child(name)
                         .child("results")
                         .child(key)
                         .awaitValue[CommandResult]
                         .map(value => if (!value.ok) throw new RequestFailedException(name, command) else value.data)
                )
        }
    }
}

