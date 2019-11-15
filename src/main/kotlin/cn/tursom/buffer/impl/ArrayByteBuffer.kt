package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.MultipleByteBuffer

class ArrayByteBuffer(
  override val buffers: Array<out ByteBuffer>
) : MultipleByteBuffer, List<ByteBuffer> by buffers.asList()