package com.dmitryegorov.hellfire

import com.firebase.client.DataSnapshot

sealed trait Event
case class Changed(ds: DataSnapshot) extends Event
case class Removed(ds: DataSnapshot) extends Event
case class Added(ds: DataSnapshot) extends Event
