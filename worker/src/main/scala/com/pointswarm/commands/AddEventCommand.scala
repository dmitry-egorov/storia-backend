package com.pointswarm.commands

import com.pointswarm.common.dtos._

case class AddEventCommand(title: Name)
{
    assert(title != null)
}
