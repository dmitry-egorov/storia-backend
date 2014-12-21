package com.pointswarm.common.views

import com.pointswarm.common.dtos.Name

case class ProfileView(name: Name, image: Option[String], bio: String)
{
    assert(name != null)
    assert(image != null)
    assert(bio != null)
}
