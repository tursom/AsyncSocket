package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer

class ThreadLocalMemoryPool(private val poolFactory: () -> MemoryPool) : MemoryPool {
  private val threadLocal = ThreadLocal<MemoryPool>()

  override fun free(token: Int) = throw NotImplementedError("ExpandableMemoryPool won't allocate any memory")

  override fun getMemory(): ByteBuffer = getPool().getMemory()

  override fun getMemoryOrNull(): ByteBuffer? = getPool().getMemoryOrNull()

  override fun toString(): String {
    return "ThreadLocalMemoryPool(threadLocal=$threadLocal)"
  }

  private fun getPool(): MemoryPool {
    var pool = threadLocal.get()
    if (pool == null) {
      pool = poolFactory()
      threadLocal.set(pool)
    }
    return pool
  }
}