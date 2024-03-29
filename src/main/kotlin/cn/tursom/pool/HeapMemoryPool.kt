package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.impl.HeapByteBuffer


@Suppress("MemberVisibilityCanBePrivate")
class HeapMemoryPool(
  blockSize: Int = 1024,
  blockCount: Int = 16,
  emptyPoolBuffer: (blockSize: Int) -> ByteBuffer = { HeapByteBuffer(it) }
) : AbstractMemoryPool(
  blockSize,
  blockCount,
  emptyPoolBuffer,
  HeapByteBuffer(java.nio.ByteBuffer.allocate(blockSize * blockCount))
) {
  override fun toString(): String {
    return "HeapMemoryPool(blockSize=$blockSize, blockCount=$blockCount, allocated=$allocated)"
  }
}