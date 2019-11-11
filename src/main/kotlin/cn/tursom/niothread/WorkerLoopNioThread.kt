package cn.tursom.niothread

import cn.tursom.socket.niothread.NioThread
import cn.tursom.utils.NonLockLinkedList
import java.nio.channels.Selector

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
class WorkerLoopNioThread(
  val threadName: String = "nioLoopThread",
  override val selector: Selector = Selector.open(),
  override val isDaemon: Boolean = false,
  override val workLoop: (thread: NioThread) -> Unit
) : NioThread {
  override var closed: Boolean = false

  val waitQueue = NonLockLinkedList<() -> Unit>()
  //val taskQueue = LinkedBlockingDeque<Future<Any?>>()

  override val thread = Thread {
    while (!closed) {
      try {
        workLoop(this)
      } catch (e: Exception) {
        e.printStackTrace()
      }
      while (true) try {
        (waitQueue.take() ?: break).invoke()
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  init {
    thread.name = threadName
    thread.isDaemon = isDaemon
    thread.start()
  }

  override fun execute(command: () -> Unit) {
    waitQueue.add(command)
  }

  override fun <T> submit(task: () -> T): NioThreadTaskFuture<T> {
    val f = Future<T>()
    waitQueue {
      try {
        f.resume(task())
      } catch (e: Throwable) {
        f.resumeWithException(e)
      }
    }
    return f
  }

  override fun close() {
    closed = true
  }

  override fun wakeup() {
    if (Thread.currentThread() != thread) {
      selector.wakeup()
    }
  }

  class Future<T> : NioThreadTaskFuture<T> {
    private val lock = Object()
    private var exception: Throwable? = null
    private var result: Pair<T, Boolean>? = null

    @Throws(Throwable::class)
    override fun get(): T {
      val result = this.result
      return when {
        exception != null -> throw exception as Throwable
        result != null -> result.first
        else -> synchronized(lock) {
          lock.wait()
          val exception = this.exception
          if (exception != null) {
            throw exception
          } else {
            this.result!!.first
          }
        }
      }
    }

    fun resume(value: T) {
      result = value to true
      synchronized(lock) {
        lock.notifyAll()
      }
    }

    fun resumeWithException(e: Throwable) {
      exception = e
      synchronized(lock) {
        lock.notifyAll()
      }
    }
  }
}