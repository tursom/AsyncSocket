package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.impl.DirectByteBuffer
import cn.tursom.buffer.impl.HeapByteBuffer


class DirectMemoryPool(
    blockSize: Int = 1024,
    blockCount: Int = 16,
    emptyBuffer: (blockSize: Int) -> ByteBuffer = { HeapByteBuffer(blockSize) }
) : AbstractMemoryPool(
    blockSize,
    blockCount,
    emptyBuffer,
    DirectByteBuffer(java.nio.ByteBuffer.allocateDirect(blockSize * blockCount))
) {
  override fun toString(): String {
    return "DirectMemoryPool(blockSize=$blockSize, blockCount=$blockCount, allocated=$allocated)"
  }
}