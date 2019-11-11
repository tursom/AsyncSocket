package cn.tursom.socket

import cn.tursom.pool.MemoryPool

class BufferedNioSocket(
  val socket: AsyncSocket,
  override val pool: MemoryPool
) : BufferedAsyncSocket, AsyncSocket by socket