package cn.tursom.socket

import cn.tursom.niothread.WorkerLoopNioThread
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.channels.SelectableChannel
import java.nio.channels.SelectionKey
import java.nio.channels.SocketChannel
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Suppress("MemberVisibilityCanBePrivate")
object AsyncNioClient {
    private const val TIMEOUT = 1000L
    private val protocol = AsyncNioSocket.nioSocketProtocol
    @JvmStatic
    private val nioThread = WorkerLoopNioThread("nioClient", isDaemon = true) { nioThread ->
        val selector = nioThread.selector
        if (selector.select(TIMEOUT) != 0) {
            val keyIter = selector.selectedKeys().iterator()
            while (keyIter.hasNext()) {
                val key = keyIter.next()
                keyIter.remove()
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
    }

    suspend fun connect(host: String, port: Int, timeout: Long = 0): AsyncNioSocket {
        val key: SelectionKey = suspendCoroutine { cont ->
            val channel = getConnection(host, port)
            val timeoutTask = if (timeout > 0) AsyncNioSocket.timer.exec(timeout) {
                channel.close()
                cont.resumeWithException(TimeoutException())
            } else {
                null
            }
            nioThread.register(channel, 0) { key ->
                timeoutTask?.cancel()
                cont.resume(key)
            }
        }
        return AsyncNioSocket(key, nioThread)
    }

    private fun getConnection(host: String, port: Int): SelectableChannel {
        val channel = SocketChannel.open()!!
        if (!channel.connect(InetSocketAddress(host, port))) {
            throw SocketException("connection failed")
        }
        channel.configureBlocking(false)
        return channel
    }
}