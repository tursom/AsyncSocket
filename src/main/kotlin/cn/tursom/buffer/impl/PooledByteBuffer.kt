package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import cn.tursom.pool.MemoryPool
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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
  private val open = AtomicBoolean(true)
  private val childCount = AtomicInteger(0)
  override val resized get() = buffer.resized

  override val closed: Boolean get() = !open.get() && !resized
  override fun close() {
    if (childCount.get() == 0) {
      if (open.compareAndSet(true, false)) {
        pool.free(token)
      }
    }
  }

  override fun resize(newSize: Int): Boolean {
    val successful = buffer.resize(newSize)
    if (successful) {
      close()
    }
    return successful
  }

  override fun slice(offset: Int, size: Int): ByteBuffer {
    return SplitByteBuffer(this, childCount, buffer.slice(offset, size))
  }

  override fun toString(): String {
    return "PooledByteBuffer(buffer=$buffer, pool=$pool, token=$token, open=$open)"
  }

  protected fun finalize() {
    pool.free(token)
  }
}