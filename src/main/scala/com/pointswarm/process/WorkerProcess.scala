package com.pointswarm.process

import com.firebase.client.Firebase
import com.pointswarm.extensions.FirebaseExtensions.FirebaseEx
import com.pointswarm.helpers.SystemEx

object WorkerProcess
{
    def main(args: Array[String])
    {
        println("Starting...")

        val ref = new Firebase("https://storia-dev.firebaseio.com")
        ref.child("commands").child("addEvent").onValueAdded((ds, s) =>
        {
            val key = ds.getKey
            val value = ds.getValue

            println(s"Recieved: $key, $value")
        })

        SystemEx.waitForShutdown

        println("Shutting down...")
    }
}


