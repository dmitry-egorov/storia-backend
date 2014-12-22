package com.dmitryegorov.tools.extensions

object ListExtensions {
    implicit class ListTupleEx[T1, T2](val l: List[(T1, T2)]) extends AnyVal {
        def asMap = l.foldLeft(Map.empty[T1, T2])((map, i) => map.updated(i._1, i._2))
    }
}

object OptionExtensions {
    implicit class BooleanEx(val b: Boolean) extends AnyVal {
        def option[A](a: => A): Option[A] = if (b) Some(a) else None
        def option: Option[Unit] = if (b) Some(()) else None
    }
}
