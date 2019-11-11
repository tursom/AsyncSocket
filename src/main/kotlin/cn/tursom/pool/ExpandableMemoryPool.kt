package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

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
    return newPool()
  }

  override fun getMemoryOrNull(): ByteBuffer? = getMemory()

  override fun toString(): String {
    return "ExpandableMemoryPool(poolList=$poolList, usingPool=$usingPool)"
  }

  private val poolLock = AtomicBoolean(false)
  @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
  private fun newPool(): ByteBuffer {
    return if (poolLock.compareAndSet(false, true)) {
      val newPool = poolFactory()
      poolList.add(newPool)
      poolLock.set(false)
      usingPool = poolList.size - 1
      synchronized(poolLock) {
        (poolLock as Object).notifyAll()
      }
      newPool.getMemory()
    } else {
      synchronized(poolLock) {
        (poolLock as Object).wait(500)
      }
      poolList.last().getMemory()
    }
  }
}