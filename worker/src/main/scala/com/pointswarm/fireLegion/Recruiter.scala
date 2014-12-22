package com.pointswarm.fireLegion

import com.firebase.client.Firebase
import com.pointswarm.fireLegion.interfaces.{Conqueror, Summoner}
import org.json4s.Formats

import scala.concurrent._

case class Recruiter[TCommand <: AnyRef : Manifest](minion: Minion[TCommand]) extends Summoner {
    def summonConqueror(fb: Firebase)(implicit ec: ExecutionContext, f: Formats): Conqueror = {
        new Commander(fb, minion)
    }
}


