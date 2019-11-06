package cn.tursom.pool

import cn.tursom.buffer.ByteBuffer
import cn.tursom.buffer.impl.DirectByteBuffer
import cn.tursom.utils.ArrayBitSet


class DirectMemoryPool(override val blockSize: Int = 1024, override val blockCount: Int = 16) : MemoryPool {
    private val memoryPool = DirectByteBuffer(java.nio.ByteBuffer.allocateDirect(blockSize * blockCount))
    private val bitMap = ArrayBitSet(blockCount.toLong())

    /**
     * @return token
     */
    override fun allocate(): Int = synchronized(this) {
        val index = bitMap.firstDown()
        if (index in 0 until blockCount) {
            bitMap.up(index)
            index.toInt()
        } else {
            -1
        }
    }

    override fun free(token: Int) {
        if (token in 0 until blockCount) synchronized(this) {
            bitMap.down(token.toLong())
        }
    }

    override fun getMemory(token: Int): ByteBuffer? = if (token in 0 until blockCount) {
        synchronized(this) {
            return memoryPool.slice(token * blockSize, (token + 1) * blockSize)
        }
    } else {
        null
    }

    override fun toString(): String {
        return "DirectMemoryPool(blockSize=$blockSize, blockCount=$blockCount, bitMap=$bitMap)"
    }
}