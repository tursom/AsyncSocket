package cn.tursom

import cn.tursom.buffer.impl.PooledByteBuffer
import cn.tursom.pool.DirectMemoryPool
import cn.tursom.pool.ExpandableMemoryPool
import cn.tursom.pool.LongBitSetDirectMemoryPool
import cn.tursom.socket.NioClient
import cn.tursom.socket.server.BuffedNioServer
import cn.tursom.utils.CurrentTimeMillisClock
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.SocketException
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.log

fun log(log: String) {
  println("${CurrentTimeMillisClock.now}: $log")
}

fun main() {
  // 服务器端口，可任意指定
  val port = 12346

  // 创建一个直接内存池，每个块是1024字节，共有10240个块
  //val memoryPool = DirectMemoryPool(1024, 10240)
  // 创建服务器对象
  val server = BuffedNioServer(port, ExpandableMemoryPool { LongBitSetDirectMemoryPool(1024) }) {
    //log("get new connection")
    // 这里处理业务逻辑，套接字对象被以 this 的方式传进来
    // 从内存池中获取一个内存块
    // 检查是否获取成功，不成功就创建一个堆缓冲
    try {
      while (true) {
        // 从套接字中读数据，五秒之内没有数据就抛出异常
        val buffer = read(10_000)
        // 输出读取到的数据
        //log("server recv from ${channel.remoteAddress}: [${buffer.readable}] ${buffer.toString(buffer.readable)}")
        //println((buffer as PooledByteBuffer).token)
        // 原封不动的返回数据
        val writeSize = write(buffer)
        //log("server send [$writeSize] bytes")
        buffer.close()
      }
    } catch (e: TimeoutException) {
      Exception(e).printStackTrace()
    } catch (e: SocketException) {
    }
    // 代码块结束后，框架会自动释放连接
  }
  server.run()

  val connectionCount = 1000
  val dataPerConn = 1000
  val testData = "testData".toByteArray()

  //val remain = AtomicInteger(connectionCount * dataPerConn)
  val remain = AtomicInteger(connectionCount)

  val clientMemoryPool = DirectMemoryPool(1024, connectionCount)

  val start = CurrentTimeMillisClock.now

  repeat(connectionCount) {
    GlobalScope.launch {
      val socket = NioClient.connect("127.0.0.1", port)
      clientMemoryPool { buffer ->
        // 检查是否获取成功，不成功就创建一个堆缓冲
        try {
          //val buffer = ByteArrayAdvanceByteBuffer(1024)
          repeat(dataPerConn) {
            buffer.clear()
            buffer.put(testData)
            //log("client sending: [${buffer.readable}] ${buffer.toString(buffer.readable)}")
            val writeSize = socket.write(buffer)
            if (writeSize == 0) {
              System.err.println("write size is zero")
            } else if (writeSize < 0) {
              return@clientMemoryPool
            }
            //log("client write [$writeSize] bytes")
            //log(buffer.toString())
            val readSize = socket.read(buffer)
            //log(buffer.toString())
            //log("client recv: [$readSize:${buffer.readable}] ${buffer.toString(buffer.readable)}")
          }
        } catch (e: Exception) {
          Exception(e).printStackTrace()
        } finally {
          remain.decrementAndGet()
          socket.close()
        }
      }
    }
  }

  while (remain.get() != 0) {
    log(remain.get().toString())
    Thread.sleep(500)
  }

  val end = CurrentTimeMillisClock.now
  println(end - start)
  server.close()
}