package com.scalasourcing.backend.memory

import com.scalasourcing.backend.EventStorageSuite

class SingleThreadInMemoryEventStorageSuite extends EventStorageSuite
{
    override def createStorage = new SingleThreadInMemoryEventStorage
    override val testMultiThreading = false
}




