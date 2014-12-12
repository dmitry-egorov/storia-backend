package com.pointswarm.tools.processing

import com.firebase.client.Firebase
import com.pointswarm.tools.processing.interfaces.{Conqueror, Summoner}
import org.json4s.Formats

import scala.concurrent._

case class Recruiter[TCommand <: AnyRef](minion: Minion[TCommand])(implicit m: Manifest[TCommand]) extends Summoner
{
    def summonConqueror(fb: Firebase)(implicit ec: ExecutionContext, f: Formats): Conqueror =
    {
        new Commander(fb, minion)
    }
}


