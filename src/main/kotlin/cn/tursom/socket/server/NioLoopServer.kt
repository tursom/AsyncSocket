package cn.tursom.socket.server

import cn.tursom.niothread.WorkerLoopNioThread
import cn.tursom.socket.NioProtocol
import cn.tursom.socket.niothread.NioThread
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.ServerSocketChannel
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * 工作在单线程上的 Nio 服务器。
 * 祖传代码，勿动
 */
class NioLoopServer(
  override val port: Int,
  private val protocol: NioProtocol,
  backLog: Int = 50,
  val nioThreadGenerator: (threadName: String, workLoop: (thread: NioThread) -> Unit) -> NioThread
) : SocketServer {
  private val listenChannel = ServerSocketChannel.open()
  private val threadList = ConcurrentLinkedDeque<NioThread>()

  init {
    listenChannel.socket().bind(InetSocketAddress(port), backLog)
    listenChannel.configureBlocking(false)
  }

  constructor(
    port: Int,
    protocol: NioProtocol,
    backLog: Int = 50
  ) : this(port, protocol, backLog, { name, workLoop ->
    WorkerLoopNioThread(name, workLoop = workLoop, isDaemon = false)
  })

  override fun run() {
    val nioThread = nioThreadGenerator("nio worker", LoopHandler(protocol)::handle)
    nioThread.register(listenChannel, SelectionKey.OP_ACCEPT) {}
    threadList.add(nioThread)
  }

  override fun close() {
    listenChannel.close()
    threadList.forEach {
      it.close()
    }
  }

  class LoopHandler(private val protocol: NioProtocol) {
    fun handle(nioThread: NioThread) {
      val selector = nioThread.selector
      if (selector.isOpen) {
        if (selector.select(TIMEOUT) != 0) {
          val keyIter = selector.selectedKeys().iterator()
          while (keyIter.hasNext()) {
            val key = keyIter.next()
            keyIter.remove()
            try {
              when {
                key.isAcceptable -> {
                  val serverChannel = key.channel() as ServerSocketChannel
                  var channel = serverChannel.accept()
                  while (channel != null) {
                    channel.configureBlocking(false)
                    nioThread.register(channel, 0) {
                      protocol.handleConnect(it, nioThread)
                    }
                    channel = serverChannel.accept()
                  }
                }
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
      }
    }
  }


  protected fun finalize() {
    close()
  }

  companion object {
    private const val TIMEOUT = 1000L
  }
}