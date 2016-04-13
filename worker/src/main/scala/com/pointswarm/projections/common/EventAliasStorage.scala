package com.pointswarm.projections.common

import com.dmitryegorov.hellfire.Hellfire._
import com.firebase.client.Firebase
import com.pointswarm.common.dtos.Alias
import com.pointswarm.domain.common.EventIdAgg
import org.json4s.Formats

import scala.concurrent._
import scala.concurrent.duration._

class EventAliasStorage(fb: Firebase)(implicit f: Formats, ec: ExecutionContext)
{
    private lazy val eventsRef: Firebase = fb / "eventAliases"
    private def eventRefOf(eventId: EventIdAgg): Firebase = eventsRef / eventId

    def getAliasOf(id: EventIdAgg): Future[Alias] =
    {
        eventRefOf(id).awaitValue[Alias](10 seconds)
    }
    
    def save(id: EventIdAgg, alias: Alias): Future[Unit] =
    {
        (eventRefOf(id) <-- alias).map(_ => ())
    }
}
