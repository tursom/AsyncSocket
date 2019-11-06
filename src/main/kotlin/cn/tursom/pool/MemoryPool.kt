package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer

/**
 * 内存池
 */
interface MemoryPool {
    val blockSize: Int
    val blockCount: Int

    fun allocate(): Int
    fun free(token: Int)
    fun getMemory(token: Int): ByteBuffer?

    override fun toString(): String

    suspend operator fun <T> invoke(action: suspend (ByteBuffer?) -> T): T {
        val token = allocate()
        return try {
            action(getMemory(token))
        } finally {
            free(token)
        }
    }
}

inline fun <T> MemoryPool.memory(action: (ByteBuffer?) -> T): T {
    val token = allocate()
    return try {
        action(getMemory(token))
    } finally {
        free(token)
    }
}