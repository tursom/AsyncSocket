package cn.tursom

import cn.tursom.pool.DirectMemoryPool
import cn.tursom.socket.NioClient
import cn.tursom.socket.server.BuffedNioServer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicInteger

fun main() {
  // 服务器端口，可任意指定
  val port = 12345

  // 创建一个直接内存池，每个块是1024字节，共有10240个块
  //val memoryPool = DirectMemoryPool(1024, 10240)
  // 创建服务器对象
  val server = BuffedNioServer(port, 1024, 10240) { pool ->
    //log("get new connection")
    // 这里处理业务逻辑，套接字对象被以 this 的方式传进来
    // 从内存池中获取一个内存块
    // 检查是否获取成功，不成功就创建一个堆缓冲
    try {
      while (true) {
        // 从套接字中读数据，五秒之内没有数据就抛出异常
        val buffer = read(pool, 10_000)
        // 输出读取到的数据
        //log("server recv from ${channel.remoteAddress}: [${buffer.readableSize}] ${buffer.toString(buffer.readableSize)}")
        // 原封不动的返回数据
        val writeSize = write(buffer)
        //log("server send [$writeSize] bytes")
        buffer.close()
      }
    } catch (e: TimeoutException) {
      Exception(e).printStackTrace()
    }
    // 代码块结束后，框架会自动释放连接
  }
  server.run()

  val connectionCount = 5000
  val dataPerConn = 30
  val testData = "testData".toByteArray()

  val remain = AtomicInteger(connectionCount * dataPerConn)

  val clientMemoryPool = DirectMemoryPool(1024, connectionCount)

  val start = System.currentTimeMillis()

  repeat(connectionCount) {
    GlobalScope.launch {
      val socket = NioClient.connect("127.0.0.1", port)
      clientMemoryPool {
        // 检查是否获取成功，不成功就创建一个堆缓冲
        try {
          val buffer = it!!
          //val buffer = ByteArrayAdvanceByteBuffer(1024)
          repeat(dataPerConn) {
            buffer.clear()
            buffer.put(testData)
            //log("client sending: [${buffer.readableSize}] ${buffer.toString(buffer.readableSize)}")
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
            //log("client recv: [$readSize:${buffer.readableSize}] ${buffer.toString(buffer.readableSize)}")
            remain.decrementAndGet()
          }
        } catch (e: Exception) {
          Exception(e).printStackTrace()
        } finally {
          socket.close()
        }
      }
    }
  }

  while (remain.get() != 0) {
    println(remain.get())
    Thread.sleep(500)
  }

  val end = System.currentTimeMillis()
  println(end - start)
  server.close()
}