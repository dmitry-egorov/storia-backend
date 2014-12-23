package com.dmitryegorov.hellfire

import com.firebase.client.DataSnapshot

sealed trait SnapEvent
case class SnapAdded(ds: DataSnapshot) extends SnapEvent
case class SnapChanged(ds: DataSnapshot) extends SnapEvent
case class SnapRemoved(ds: DataSnapshot) extends SnapEvent

sealed trait DataEvent
case class DataAdded[I, V](id: I, value: V) extends DataEvent
case class DataChanged[I, V](id: I, value: V) extends DataEvent
case class DataRemoved[I](id: I) extends DataEvent
