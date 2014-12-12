package com.pointswarm.tools.hellfire.events

import com.firebase.client.DataSnapshot

case class Changed(ds: DataSnapshot) extends Event
