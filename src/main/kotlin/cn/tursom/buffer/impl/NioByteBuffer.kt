package cn.tursom.buffer.impl

import cn.tursom.buffer.ByteBuffer


class NioByteBuffer(buffer: java.nio.ByteBuffer) :
    ByteBuffer by if (buffer.hasArray()) {
        HeapByteBuffer(buffer)
    } else {
        DirectByteBuffer(buffer)
    }