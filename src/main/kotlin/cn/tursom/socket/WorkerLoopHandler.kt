package cn.tursom.socket

import cn.tursom.socket.niothread.NioThread
import java.nio.channels.SelectionKey

class WorkerLoopHandler(private val protocol: NioProtocol) {
  fun handle(nioThread: NioThread, key: SelectionKey) {
    try {
      when {
        key.isReadable -> {
          protocol.handleRead(key, nioThread)
        }
        key.isWritable -> {
          protocol.handleWrite(key, nioThread)
        }
      }
    } catch (e: Throwable) {
      try {
        protocol.exceptionCause(key, nioThread, e)
      } catch (e1: Throwable) {
        e.printStackTrace()
        e1.printStackTrace()
        key.cancel()
        key.channel().close()
      }
    }
  }
}