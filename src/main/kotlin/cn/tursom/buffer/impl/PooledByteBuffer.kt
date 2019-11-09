package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import cn.tursom.pool.MemoryPool

class PooledByteBuffer(
    private val buffer: ByteBuffer,
    private val pool: MemoryPool,
    val token: Int
) : ByteBuffer by buffer {
  /**
   * 这个变量保证 buffer 不会被释放多次
   */
  private var open: Boolean = true

  override val closed: Boolean get() = !open
  override fun close() {
    if (open) {
      open = false
      pool.free(token)
    }
  }

  override fun toString(): String {
    return "PooledByteBuffer(buffer=$buffer, pool=$pool, token=$token, open=$open)"
  }
}