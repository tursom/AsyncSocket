package cn.tursom.socket.server

import cn.tursom.pool.*
import cn.tursom.socket.NioSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

/**
 * 带内存池的 NIO 套接字服务器。
 * 在处理结束后会自动释放由内存池分配的内存
 */
class BuffedNioServer(
  port: Int,
  memoryPool: MemoryPool,
  backlog: Int = 50,
  coroutineScope: CoroutineScope = GlobalScope,
  handler: suspend NioSocket.(memoryPool: MarkedMemoryPool) -> Unit
) : NioServer(port, backlog, coroutineScope, {
  MarkedMemoryPool(memoryPool).use { marked ->
    handler(marked)
  }
}) {
  constructor(
    port: Int,
    blockSize: Int = 1024,
    blockCount: Int = 128,
    backlog: Int = 50,
    coroutineScope: CoroutineScope = GlobalScope,
    handler: suspend NioSocket.(buffer: MarkedMemoryPool) -> Unit
  ) : this(
    port,
    ThreadLocalMemoryPool { ExpandableMemoryPool { ThreadUnsafeDirectMemoryPool(blockSize, blockCount) } },
    backlog,
    coroutineScope,
    handler
  )
}