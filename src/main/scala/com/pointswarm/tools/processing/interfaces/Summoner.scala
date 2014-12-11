package com.pointswarm.tools.processing.interfaces

import com.firebase.client.Firebase
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import org.json4s.Formats

import scala.concurrent.ExecutionContext

trait Summoner
{
    def summonConqueror(commandsRef: Firebase, token: CancellationToken)(implicit ec: ExecutionContext, f: Formats): Conqueror
}
