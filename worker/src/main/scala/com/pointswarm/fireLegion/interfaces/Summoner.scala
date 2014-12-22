package com.pointswarm.fireLegion.interfaces

import com.firebase.client.Firebase
import org.json4s.Formats

import scala.concurrent.ExecutionContext

trait Summoner {
    def summonConqueror(commandsRef: Firebase)(implicit ec: ExecutionContext, f: Formats): Conqueror
}
