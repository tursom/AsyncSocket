package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer

/**
 * 内存池
 */
interface MemoryPool {
  val staticSize: Boolean get() = true

  //  fun allocate(): Int
  fun free(token: Int)

  fun getMemory(): ByteBuffer
  fun getMemoryOrNull(): ByteBuffer?

  override fun toString(): String

  suspend operator fun <T> invoke(action: suspend (ByteBuffer?) -> T): T {
    return getMemory().use { buffer ->
      action(buffer)
    }
  }

  fun get() = getMemory()

  operator fun get(blockCount: Int): Array<ByteBuffer> = Array(blockCount) { get() }
}

inline fun <T> MemoryPool.memory(action: (ByteBuffer?) -> T): T {
  return getMemory().use { buffer ->
    action(buffer)
  }
}
