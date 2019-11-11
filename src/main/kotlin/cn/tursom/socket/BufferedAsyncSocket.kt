package cn.tursom.socket

import cn.tursom.buffer.ByteBuffer
import cn.tursom.pool.MemoryPool

interface BufferedAsyncSocket : AsyncSocket {
  val pool: MemoryPool
  suspend fun read(timeout: Long = 0L): ByteBuffer = read(pool, timeout)
}