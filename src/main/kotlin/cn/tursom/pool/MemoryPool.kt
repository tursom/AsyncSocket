package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer

/**
 * 内存池
 */
interface MemoryPool {
  val blockSize: Int
  val blockCount: Int
  val allocated: Int

  fun allocate(): Int
  fun free(token: Int)
  fun getMemory(token: Int): ByteBuffer

  override fun toString(): String

  suspend operator fun <T> invoke(action: suspend (ByteBuffer?) -> T): T {
    val token = allocate()
    return try {
      action(getMemory(token))
    } finally {
      free(token)
    }
  }

  fun get() = getMemory(allocate())

  operator fun get(blockCount: Int): Array<ByteBuffer> = Array(blockCount) { get() }
}

inline fun <T> MemoryPool.memory(action: (ByteBuffer?) -> T): T {
  val token = allocate()
  return try {
    action(getMemory(token))
  } finally {
    free(token)
  }
}
