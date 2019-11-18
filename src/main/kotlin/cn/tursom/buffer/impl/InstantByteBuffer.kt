package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.ProxyByteBuffer
import cn.tursom.pool.MemoryPool

class InstantByteBuffer(
  override val agent: ByteBuffer,
  val pool: MemoryPool
) : ProxyByteBuffer, ByteBuffer by agent {
  override var closed = false

  override fun close() {
    agent.close()
    pool.free(this)
    closed = true
  }

  override fun toString() = "InstantByteBuffer(agent=$agent)"
}