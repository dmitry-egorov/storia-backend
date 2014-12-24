package com.dmitryegorov.hellfire

import com.firebase.client.DataSnapshot

sealed trait SnapEvent
case class SnapAdded(ds: DataSnapshot) extends SnapEvent
case class SnapChanged(ds: DataSnapshot) extends SnapEvent
case class SnapRemoved(ds: DataSnapshot) extends SnapEvent

sealed trait DataEvent
case class DataAdded[V](id: String, value: V) extends DataEvent
case class DataChanged[V](id: String, value: V) extends DataEvent
case class DataRemoved(id: String) extends DataEvent
