package com.pointswarm.fireLegion.test

import com.dmitryegorov.hellfire.Hellfire._
import com.dmitryegorov.futuristic.cancellation.CancellationToken
import com.dmitryegorov.tools.extensions.StringExtensions._
import com.firebase.client.Firebase
import com.pointswarm.fireLegion._
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

trait MinionTest
{
    def execute[Command <: AnyRef : Manifest](fb: Firebase, minion: Minion[Command], command: Command)(implicit ec: ExecutionContext, f: Formats): Future[Unit] =
    {
        val army = Master(fb).recruit(minion).createArmy

        army.conquer(CancellationToken.none)

        val minionRef = fb / "minions" / minion.getClass.getSimpleName.decapitalize

        (minionRef / "inbox" / "1" <-- command)
        .flatMap(_ => (minionRef / "results" / "1" / "data").await().map(_ => ()))
    }
}
