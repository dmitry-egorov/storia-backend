package com.pointswarm.tools.processing

import com.firebase.client.Firebase
import com.pointswarm.tools.processing.interfaces.Summoner
import org.json4s.Formats

import scala.concurrent._

object Master
{
    def apply(): Master = new Master(Nil)
}

class Master(summoners: List[Summoner])
{
    def createArmy(fb: Firebase)(implicit ec: ExecutionContext, f: Formats): Army =
    {
        val commanders =
            summoners
            .map(c => c.summonConqueror(fb))
            .toList

        new Army(commanders)
    }

    def recruit[TCommand <: AnyRef](minion: Minion[TCommand])(implicit m: Manifest[TCommand]): Master =
    {
        val recruiter = Recruiter[TCommand](minion)

        val newRecruiters = recruiter :: summoners

        new Master(newRecruiters)
    }
}
