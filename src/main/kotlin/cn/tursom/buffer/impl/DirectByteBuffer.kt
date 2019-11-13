package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.read

class DirectByteBuffer(private var buffer: java.nio.ByteBuffer) : ByteBuffer {
  constructor(size: Int) : this(java.nio.ByteBuffer.allocateDirect(size))

  override val hasArray: Boolean = false
  override val array: ByteArray get() = buffer.array()
  override val capacity: Int get() = buffer.capacity()
  override val arrayOffset: Int = 0
  override var writePosition: Int = 0
  override var readPosition: Int = 0

  override fun readBuffer(): java.nio.ByteBuffer {
    if (buffer.limit() != writePosition)
      buffer.limit(writePosition)
    if (buffer.position() != readPosition)
      buffer.position(readPosition)
    return buffer
  }

  override fun finishRead(buffer: java.nio.ByteBuffer) {
    readPosition = buffer.position()
  }

  override fun writeBuffer(): java.nio.ByteBuffer {
    if (buffer.limit() != capacity)
      buffer.limit(capacity)
    if (buffer.position() != writePosition)
      buffer.position(writePosition)
    return buffer
  }

  override fun finishWrite(buffer: java.nio.ByteBuffer) {
    writePosition = buffer.position()
  }

  override fun reset() {
    buffer.limit(writePosition)
    buffer.position(readPosition)
    buffer.compact()
    readPosition = buffer.position()
    writePosition = buffer.limit()
  }

  override fun slice(offset: Int, size: Int): ByteBuffer {
    buffer.limit(offset + size)
    buffer.position(offset)
    return DirectByteBuffer(buffer.slice())
  }

  override fun resize(newSize: Int): Boolean {
    if (newSize <= buffer.capacity()) return false
    val newBuf = java.nio.ByteBuffer.allocateDirect(newSize)
    newBuf.put(readBuffer())
    buffer = newBuf
    writePosition = readable
    readPosition = 0
    return true
  }

  override fun toString(): String {
    return "DirectByteBuffer(buffer=$buffer, hasArray=$hasArray, array=${array.contentToString()
    }, capacity=$capacity, writePosition=$writePosition, readPosition=$readPosition)"
  }
}