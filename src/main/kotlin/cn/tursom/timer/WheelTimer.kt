package cn.tursom.timer

import java.lang.Thread.sleep
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlin.concurrent.thread


@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class WheelTimer(
  val tick: Long = 200,
  val wheelSize: Int = 512,
  val name: String = "wheelTimerLooper",
  val taskQueueFactory: () -> TaskQueue = { NonLockTaskQueue() }
) : Timer {
  var closed = false
  val taskQueueArray = Array(wheelSize) { taskQueueFactory() }
  private var position = 0

  override fun exec(timeout: Long, task: () -> Unit): TimerTask {
    val index = ((timeout / tick + position + if (timeout % tick == 0L) 0 else 1) % wheelSize).toInt()
    return taskQueueArray[index].offer(task, timeout)
  }

  init {
    thread(isDaemon = true, name = name) {
      val startTime = System.currentTimeMillis()
      while (!closed) {
        position %= wheelSize

        val newQueue = taskQueueFactory()
        val taskQueue = taskQueueArray[position]
        taskQueueArray[position] = newQueue

        val time = System.currentTimeMillis()
        var node = taskQueue.take()
        while (node != null) {
          if (!node.canceled && node.isOutTime(time)) {
            val sNode = node
            threadPool.execute { sNode.task() }
          } else {
            newQueue.offer(node)
          }
          node = taskQueue.take()
        }

        position++
        val nextSleep = startTime + tick * position - System.currentTimeMillis()
        if (nextSleep > 0) sleep(tick)
      }
    }
  }

  companion object {
    val threadPool: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
      object : ThreadFactory {
        var threadNumber = 0
        override fun newThread(r: Runnable): Thread {
          val thread = Thread(r)
          thread.isDaemon = true
          thread.name = "wheelTimerWorker-$threadNumber"
          return thread
        }
      })
    val timer by lazy { WheelTimer(200, 1024) }
    val smoothTimer by lazy { WheelTimer(20, 128) }
  }
}