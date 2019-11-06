package cn.tursom.socket.server

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.impl.HeapByteBuffer
import cn.tursom.pool.DirectMemoryPool
import cn.tursom.pool.MemoryPool
import cn.tursom.pool.memory
import cn.tursom.socket.NioSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope

/**
 * 带内存池的 NIO 套接字服务器。<br />
 * 其构造函数是标准写法的改造，会向 handler 方法传入一个 AdvanceByteBuffer，默认是 DirectAdvanceByteBuffer，
 * 当内存池用完之后会换为 ByteArrayAdvanceByteBuffer。
 */
class BuffedNioServer(
    port: Int,
    memoryPool: MemoryPool,
    backlog: Int = 50,
    coroutineScope: CoroutineScope = GlobalScope,
    handler: suspend NioSocket.(buffer: ByteBuffer) -> Unit
) : NioServer(port, backlog, coroutineScope, {
    memoryPool.memory {
        handler(it ?: HeapByteBuffer(memoryPool.blockSize))
    }
}) {
    constructor(
        port: Int,
        blockSize: Int = 1024,
        blockCount: Int = 128,
        backlog: Int = 50,
        coroutineScope: CoroutineScope = GlobalScope,
        handler: suspend NioSocket.(buffer: ByteBuffer) -> Unit
    ) : this(port, DirectMemoryPool(blockSize, blockCount), backlog, coroutineScope, handler)
}