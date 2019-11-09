package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.impl.PooledByteBuffer
import java.io.Closeable

class MarkedMemoryPool(private val pool: MemoryPool) : MemoryPool by pool, Closeable {
  private val allocatedList = ArrayList<ByteBuffer>(2)
  override fun getMemory(token: Int): ByteBuffer {
    val memory = pool.getMemory(token)
    allocatedList.add(memory)
    return memory
  }

  override fun close() {
    allocatedList.forEach(ByteBuffer::close)
    allocatedList.clear()
  }

  override fun toString(): String {
    val allocated = ArrayList<Int>(allocatedList.size)
    allocatedList.forEach {
      if (it is PooledByteBuffer && !it.closed) allocated.add(it.token)
    }
    return "MarkedMemoryPool(pool=$pool, allocated=$allocated)"
  }
}