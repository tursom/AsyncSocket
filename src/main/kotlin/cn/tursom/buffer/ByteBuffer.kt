package cn.tursom.buffer

import cn.tursom.utils.forEachIndex
import java.io.Closeable
import java.io.OutputStream
import java.nio.Buffer
import kotlin.math.min

/**
 * 针对 java nio 的弱智 ByteBuffer 的简单封装
 * 支持读写 buffer 分离
 */
@Suppress("unused")
interface ByteBuffer : Closeable {
  /**
   * 使用读 buffer，ByteBuffer 实现类有义务维护指针正常推进
   */
  fun <T> readBuffer(block: (java.nio.ByteBuffer) -> T): T {
    val buffer = readBuffer()
    return try {
      block(buffer)
    } finally {
      finishRead(buffer)
    }
  }

  /**
   * 使用写 buffer，ByteBuffer 实现类有义务维护指针正常推进
   */
  fun <T> writeBuffer(block: (java.nio.ByteBuffer) -> T): T {
    val buffer = writeBuffer()
    return try {
      block(buffer)
    } finally {
      finishWrite(buffer)
    }
  }
  val readable: Int get() = read(Buffer::remaining)
  val writeable: Int get() = write(Buffer::remaining)

  val hasArray: Boolean
  val array: ByteArray

  val capacity: Int
  val arrayOffset: Int
  var writePosition: Int
  var readPosition: Int

  val closed: Boolean get() = false
  val resized: Boolean

  override fun close() {
  }

  fun readBuffer(): java.nio.ByteBuffer
  fun finishRead(buffer: java.nio.ByteBuffer)
  fun writeBuffer(): java.nio.ByteBuffer
  fun finishWrite(buffer: java.nio.ByteBuffer)

  fun reset()
  fun slice(offset: Int, size: Int): ByteBuffer

  /**
   * @return 底层 nio buffer 是否已更新
   */
  fun resize(newSize: Int): Boolean

  val writeOffset: Int get() = arrayOffset + writePosition
  val readOffset: Int get() = arrayOffset + readPosition

  fun clear() {
    readPosition = 0
    writePosition = 0
  }

  fun get(): Byte = read { it.get() }
  fun getChar(): Char = read { it.char }
  fun getShort(): Short = read { it.short }
  fun getInt(): Int = read { it.int }
  fun getLong(): Long = read { it.long }
  fun getFloat(): Float = read { it.float }
  fun getDouble(): Double = read { it.double }

  fun getBytes(size: Int = readable): ByteArray = read {
    val bytes = ByteArray(size)
    it.get(bytes)
    bytes
  }

  fun getString(size: Int = readable): String = String(getBytes(size))

  fun toString(size: Int): String {
    val bytes = getBytes(size)
    readPosition -= bytes.size
    return String(bytes)
  }

  fun writeTo(buffer: ByteArray, bufferOffset: Int = 0, size: Int = min(readable, buffer.size)): Int {
    val readSize = min(readable, size)
    if (hasArray) {
      array.copyInto(buffer, bufferOffset, readOffset, readOffset + readSize)
      readPosition += readOffset
      reset()
    } else {
      read {
        it.put(buffer, bufferOffset, readSize)
      }
    }
    return readSize
  }

  fun writeTo(os: OutputStream): Int {
    val size = readable
    if (hasArray) {
      os.write(array, readOffset, size)
      readPosition += size
      reset()
    } else {
      val buffer = ByteArray(1024)
      read {
        while (it.remaining() > 0) {
          it.put(buffer)
          os.write(buffer)
        }
      }
    }
    return size
  }

  fun writeTo(buffer: ByteBuffer): Int {
    val size = min(readable, buffer.readable)
    if (hasArray) {
      buffer.put(array, readOffset, size)
      readPosition += size
      reset()
    } else {
      read { read ->
        buffer.write { write -> write.put(read) }
      }
    }
    return size
  }

  fun toByteArray() = getBytes()


  /*
   * 数据写入方法
   */

  fun put(byte: Byte): Unit = write { it.put(byte) }
  fun put(char: Char): Unit = write { it.putChar(char) }
  fun put(short: Short): Unit = write { it.putShort(short) }
  fun put(int: Int): Unit = write { it.putInt(int) }
  fun put(long: Long): Unit = write { it.putLong(long) }
  fun put(float: Float): Unit = write { it.putFloat(float) }
  fun put(double: Double): Unit = write { it.putDouble(double) }
  fun put(str: String): Unit = put(str.toByteArray())
  fun put(buffer: ByteBuffer): Int = buffer.writeTo(this)
  fun put(byteArray: ByteArray, startIndex: Int = 0, endIndex: Int = byteArray.size - startIndex) {
    if (hasArray) {
      byteArray.copyInto(array, writeOffset, startIndex, endIndex)
      writePosition += endIndex - startIndex
    } else {
      write {
        it.put(byteArray, startIndex, endIndex - startIndex)
      }
    }
  }

  fun put(array: CharArray, index: Int = 0, size: Int = array.size - index) {
    array.forEachIndex(index, index + size - 1, this::put)
  }

  fun put(array: ShortArray, index: Int = 0, size: Int = array.size - index) {
    array.forEachIndex(index, index + size - 1, this::put)
  }

  fun put(array: IntArray, index: Int = 0, size: Int = array.size - index) {
    array.forEachIndex(index, index + size - 1, this::put)
  }

  fun put(array: LongArray, index: Int = 0, size: Int = array.size - index) {
    array.forEachIndex(index, index + size - 1, this::put)
  }

  fun put(array: FloatArray, index: Int = 0, size: Int = array.size - index) {
    array.forEachIndex(index, index + size - 1, this::put)
  }

  fun put(array: DoubleArray, index: Int = 0, size: Int = array.size - index) {
    array.forEachIndex(index, index + size - 1, this::put)
  }

  fun fill(byte: Byte) {
    readPosition = 0
    writePosition = 0
    write {
      while (it.remaining() != 0) {
        it.put(byte)
      }
    }
    writePosition = 0
  }

  fun split(maxSize: Int): Array<out ByteBuffer> {
    val size = (((capacity - 1) / maxSize) + 1).and(0x7fff_ffff)
    return Array(size) {
      if (it != size - 1) {
        slice(it * maxSize, maxSize)
      } else {
        slice(it * maxSize, capacity - it * maxSize)
      }
    }
  }
}