package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer

class ExpandableMemoryPool(private val poolFactory: () -> MemoryPool) : MemoryPool {
  private val poolList = ArrayList<MemoryPool>(1)
  private var usingPool = 0

  init {
    poolList.add(poolFactory())
  }

  override fun free(token: Int) = throw NotImplementedError("ExpandableMemoryPool won't allocate any memory")

  override fun getMemory(): ByteBuffer {
    val pool = poolList[usingPool]
    var buffer = pool.getMemoryOrNull()
    if (buffer != null) return buffer
    repeat(poolList.size) {
      buffer = poolList[it].getMemoryOrNull()
      if (buffer != null) usingPool = it
      return buffer ?: return@repeat
    }
    val newPool = poolFactory()
    poolList.add(newPool)
    return newPool.getMemory()
  }

  override fun getMemoryOrNull(): ByteBuffer? = getMemory()

  override fun toString(): String {
    return "ExpandableMemoryPool(poolList=$poolList, usingPool=$usingPool)"
  }
}