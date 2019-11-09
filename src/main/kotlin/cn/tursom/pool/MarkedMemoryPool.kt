package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import java.io.Closeable

class MarkedMemoryPool(private val pool: MemoryPool) : MemoryPool by pool, Closeable {
  private val allocatedList = ArrayList<Int>(2)
  override fun getMemory(token: Int): ByteBuffer {
    val memory = pool.getMemory(token)
    allocatedList.add(token)
    return memory
  }

  override fun close() {
    allocatedList.forEach(pool::free)
    allocatedList.clear()
  }

  override fun toString(): String {
    return "MarkedMemoryPool(pool=$pool, allocatedList=$allocatedList)"
  }
}