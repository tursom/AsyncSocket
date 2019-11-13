package cn.tursom.buffer

interface ProxyByteBuffer : ByteBuffer {
  val agent: ByteBuffer
}