package com.pointswarm.tools.hellfire.events

import com.firebase.client.DataSnapshot

case class Added(ds: DataSnapshot) extends Event
