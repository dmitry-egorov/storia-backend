package com.pointswarm.tools.hellfire

import com.firebase.client._

sealed trait Event
case class Changed(ds: DataSnapshot) extends Event
case class Removed(ds: DataSnapshot) extends Event
case class Added(ds: DataSnapshot) extends Event
