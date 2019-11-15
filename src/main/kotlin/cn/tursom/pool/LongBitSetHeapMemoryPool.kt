package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.impl.HeapByteBuffer

class LongBitSetHeapMemoryPool (
  blockSize: Int,
  emptyPoolBuffer: (blockSize: Int) -> ByteBuffer = { HeapByteBuffer(blockSize) }
) : LongBitSetAbstractMemoryPool(blockSize, emptyPoolBuffer, HeapByteBuffer(64 * blockSize)) {
  override fun toString(): String {
    return "LongBitSetDirectMemoryPool(blockSize=$blockSize, blockCount=$blockCount, allocated=$allocated)"
  }
}