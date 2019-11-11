package cn.tursom.timer

import cn.tursom.utils.NonLockLinkedList
import java.util.concurrent.*

interface Timer {
  fun exec(timeout: Long, task: () -> Unit): TimerTask

  fun runNow(task: () -> Unit) {
    threadPool.execute { task() }
  }

  companion object {
    val threadPool: ExecutorService = ThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().availableProcessors(),
        0L, TimeUnit.MILLISECONDS,
        NonLockLinkedList(),
        object : ThreadFactory {
          var threadNumber = 0
          override fun newThread(r: Runnable): Thread {
            val thread = Thread(r)
            thread.isDaemon = true
            thread.name = "timer-worker-$threadNumber"
            return thread
          }
        })
  }
}
