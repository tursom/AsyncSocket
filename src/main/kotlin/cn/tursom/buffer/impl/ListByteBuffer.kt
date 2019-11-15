package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.MultipleByteBuffer

class ListByteBuffer(bufferList: List<ByteBuffer>) : MultipleByteBuffer, List<ByteBuffer> by bufferList