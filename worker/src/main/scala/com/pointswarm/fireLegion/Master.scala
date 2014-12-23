package com.pointswarm.fireLegion

import com.firebase.client.Firebase
import com.pointswarm.fireLegion.distributor._
import com.pointswarm.fireLegion.interfaces.Summoner
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

    def recruit[TCommand <: AnyRef : Manifest](minion: Minion[TCommand]): Master =
    {
        val recruiter = Recruiter[TCommand](minion)

        val newRecruiters = recruiter :: summoners

        copy(summoners = newRecruiters)
    }
}
