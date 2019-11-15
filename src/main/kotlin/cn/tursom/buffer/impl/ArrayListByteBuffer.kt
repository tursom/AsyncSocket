package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.MultipleByteBuffer

class ArrayListByteBuffer : MultipleByteBuffer, MutableList<ByteBuffer> by ArrayList() {
  override fun clear() {
    super.clear()
    (this as MutableList<ByteBuffer>).clear()
  }
}