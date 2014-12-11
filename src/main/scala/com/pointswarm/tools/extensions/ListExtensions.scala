package com.pointswarm.tools.extensions

object ListExtensions
{
    implicit class ListTupleEx[T1, T2](l: List[(T1, T2)])
    {
        def asMap = l.foldLeft(Map.empty[T1, T2])((map, i) => map.updated(i._1, i._2))
    }
}
