package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SplitByteBuffer(
    private val parent: ByteBuffer,
    private val childCount: AtomicInteger,
    val buffer: ByteBuffer
) : ByteBuffer by buffer {
  init {
    childCount.incrementAndGet()
  }

  private val atomicClosed = AtomicBoolean(false)

  override val closed: Boolean get() = atomicClosed.get()

  override fun close() {
    if (atomicClosed.compareAndSet(false, true)) {
      super.close()
      childCount.decrementAndGet()
      if (childCount.get() == 0 && (parent.closed || parent.resized)) {
        parent.close()
      }
    }
  }

  override fun slice(offset: Int, size: Int): ByteBuffer {
    return SplitByteBuffer(parent, childCount, buffer.slice(offset, size))
  }

  override fun resize(newSize: Int): Boolean {
    val successful = buffer.resize(newSize)
    if (successful) {
      close()
    }
    return successful
  }

  protected fun finalize() {
    close()
  }
}