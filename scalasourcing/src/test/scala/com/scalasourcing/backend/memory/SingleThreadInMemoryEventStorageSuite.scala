package com.scalasourcing.backend.memory

import com.scalasourcing.backend.{Tester, EventStorageSuite}

class SingleThreadInMemoryEventStorageSuite extends EventStorageSuite
{
    override def createStorage = new SingleThreadInMemoryEventStorage {val a = Tester}
    override val testMultiThreading = false
}




