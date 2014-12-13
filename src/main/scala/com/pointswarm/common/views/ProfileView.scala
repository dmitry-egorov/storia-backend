package com.pointswarm.common.views

case class ProfileView(name: String, image: String, bio: String)
{
    assert(name != null)
    assert(image != null)
    assert(bio != null)
}
