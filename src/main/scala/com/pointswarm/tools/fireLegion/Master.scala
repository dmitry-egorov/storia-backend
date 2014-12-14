package com.pointswarm.tools.fireLegion

import com.firebase.client.Firebase
import com.pointswarm.tools.fireLegion.distributor._
import com.pointswarm.tools.fireLegion.interfaces.Summoner
import org.json4s.Formats

import scala.concurrent._

object Master
{
    def apply(fb: Firebase)(implicit ec: ExecutionContext, f: Formats): Master = Master(fb, Nil)
}

case class Master(fb: Firebase, summoners: List[Summoner])(implicit ec: ExecutionContext, f: Formats)
{
    def recruitDistributor = recruit(new Distributor(fb))

    def createArmy(implicit ec: ExecutionContext, f: Formats): Army =
    {
        val commanders =
            summoners
            .map(c => c.summonConqueror(fb))
            .toList

        Army(commanders)
    }

    def recruit[TCommand <: AnyRef](minion: Minion[TCommand])(implicit m: Manifest[TCommand]): Master =
    {
        val recruiter = Recruiter[TCommand](minion)

        val newRecruiters = recruiter :: summoners

        copy(summoners = newRecruiters)
    }
}
