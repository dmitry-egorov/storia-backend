package com.pointswarm.helpers

object SystemEx
{
    def waitForShutdown =
    {
        val shutdownLock = new Object()

        sys addShutdownHook
            {
                shutdownLock.synchronized
                {
                    shutdownLock.notify()
                }
            }

        shutdownLock.synchronized
        {
            shutdownLock.wait()
        }
    }
}
