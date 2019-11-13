package cn.tursom.buffer

interface MarkableByteBuffer : ByteBuffer {
  fun mark()
  fun resume()
}