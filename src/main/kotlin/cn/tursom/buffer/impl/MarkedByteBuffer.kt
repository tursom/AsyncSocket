package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.MarkableByteBuffer
import cn.tursom.buffer.ProxyByteBuffer

class MarkedByteBuffer(override val agent: ByteBuffer) : ProxyByteBuffer, MarkableByteBuffer, ByteBuffer by agent {
  private var writeMark = 0
  private var readMark = 0

  override fun mark() {
    writeMark = writePosition
    readMark = readPosition
  }

  override fun resume() {
    writePosition = writeMark
    readPosition = readMark
  }
}