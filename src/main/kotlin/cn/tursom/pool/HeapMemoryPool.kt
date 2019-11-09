package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.impl.DirectByteBuffer
import cn.tursom.buffer.impl.HeapByteBuffer
import cn.tursom.buffer.impl.PooledByteBuffer
import cn.tursom.utils.ArrayBitSet


@Suppress("MemberVisibilityCanBePrivate")
class HeapMemoryPool(
    blockSize: Int = 1024,
    blockCount: Int = 16,
    emptyBuffer: (blockSize: Int) -> ByteBuffer = { HeapByteBuffer(blockSize) }
) : AbstractMemoryPool(
    blockSize,
    blockCount,
    emptyBuffer,
    HeapByteBuffer(java.nio.ByteBuffer.allocate(blockSize * blockCount))
) {
  override fun toString(): String {
    return "HeapMemoryPool(blockSize=$blockSize, blockCount=$blockCount, allocated=$allocated)"
  }
}