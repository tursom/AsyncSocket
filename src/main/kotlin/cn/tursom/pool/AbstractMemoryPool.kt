package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.impl.HeapByteBuffer
import cn.tursom.buffer.impl.PooledByteBuffer
import cn.tursom.utils.ArrayBitSet

abstract class AbstractMemoryPool(
    final override val blockSize: Int,
    final override val blockCount: Int,
    val emptyBuffer: (blockSize: Int) -> ByteBuffer = { HeapByteBuffer(blockSize) },
    private val memoryPool: ByteBuffer
) : MemoryPool {
  private val bitMap = ArrayBitSet(blockCount.toLong())
  override val allocated: Int get() = bitMap.trueCount.toInt()

  private fun unsafeAllocate(): Int {
    val index = bitMap.firstDown()
    return if (index in 0 until blockCount) {
      bitMap.up(index)
      index.toInt()
    } else {
      -1
    }
  }

  private fun unsafeFree(token: Int) {
    bitMap.down(token.toLong())
  }

  private fun unsafeGetMemory(token: Int): ByteBuffer {
    return PooledByteBuffer(memoryPool.slice(token * blockSize, blockSize), this, token)
  }

  /**
   * @return token
   */
  override fun allocate(): Int = synchronized(this) { unsafeAllocate() }

  override fun free(token: Int) {
    if (token in 0 until blockCount) synchronized(this) { unsafeFree(token) }
  }

  override fun getMemory(token: Int): ByteBuffer = if (token in 0 until blockCount) {
    synchronized(this) { return unsafeGetMemory(token) }
  } else {
    emptyBuffer(blockSize)
  }

  override fun get(blockCount: Int): Array<ByteBuffer> = synchronized(this) {
    Array(blockCount) {
      val token = unsafeAllocate()
      if (token in 0 until blockCount) {
        unsafeGetMemory(token)
      } else {
        emptyBuffer(blockSize)
      }
    }
  }
}