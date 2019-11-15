package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.impl.DirectByteBuffer

class LongBitSetDirectMemoryPool(
  blockSize: Int,
  emptyPoolBuffer: (blockSize: Int) -> ByteBuffer = { DirectByteBuffer(it) }
) : LongBitSetAbstractMemoryPool(blockSize, emptyPoolBuffer, DirectByteBuffer(64 * blockSize)) {
  override fun toString(): String {
    return "LongBitSetDirectMemoryPool(blockSize=$blockSize, blockCount=$blockCount, allocated=$allocated)"
  }
}