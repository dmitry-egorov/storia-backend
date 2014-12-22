package com.scalasourcing.backend.memory

import com.scalasourcing.backend.{EventStorageSuite, Tester}

class SingleThreadInMemoryEventStorageSuite extends EventStorageSuite {
    override def createStorage = new SingleThreadInMemoryEventStorage {val a = Tester}
    override val testMultiThreading = false
}




