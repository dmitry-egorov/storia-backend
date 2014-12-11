package com.pointswarm.tools.processing

import com.firebase.client.Firebase
import com.pointswarm.tools.futuristic.cancellation.CancellationToken
import org.json4s.Formats

import scala.concurrent.{ExecutionContext, Future}

trait FireCommander
{
    def run(commandsRef: Firebase, token: CancellationToken)(implicit ec: ExecutionContext, f: Formats): Future[Int]
}


