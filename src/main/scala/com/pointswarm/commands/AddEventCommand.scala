package com.pointswarm.commands

case class AddEventCommand(title: String)
{
    assert(title != null && title.trim.nonEmpty)
}
