package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import cn.tursom.pool.MemoryPool
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 在被垃圾回收时能保证释放占用的内存池内存
 */
class PooledByteBuffer(
    private val buffer: ByteBuffer,
    private val pool: MemoryPool,
    val token: Int
) : ByteBuffer by buffer {
  /**
   * 这个变量保证 buffer 不会被释放多次
   */
  private var open = AtomicBoolean(true)

  override val closed: Boolean get() = !open.get()
  override fun close() {
    if (open.compareAndSet(true, false)) {
      pool.free(token)
    }
  }

  override fun toString(): String {
    return "PooledByteBuffer(buffer=$buffer, pool=$pool, token=$token, open=$open)"
  }

  protected fun finalize() {
    close()
  }
}