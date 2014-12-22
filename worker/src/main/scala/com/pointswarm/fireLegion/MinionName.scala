package com.pointswarm.fireLegion

import com.dmitryegorov.tools.extensions.StringExtensions._

case class MinionName(value: String) extends AnyVal {
    override def toString: String = value
}

object MinionName {
    implicit def fromString(s: String): MinionName = MinionName(s)
    implicit def toString(mn: MinionName): String = mn.value

    def apply(minion: Minion[_]): MinionName = {
        minion.getClass.getSimpleName.decapitalize
    }
}
