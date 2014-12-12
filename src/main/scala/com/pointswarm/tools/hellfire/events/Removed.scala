package com.pointswarm.tools.hellfire.events

import com.firebase.client.DataSnapshot

case class Removed(ds: DataSnapshot) extends Event
