package cn.tursom.timer

import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReferenceArray
import kotlin.concurrent.thread


@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class WheelTimer(
  val tick: Long = 200,
  val wheelSize: Int = 512,
  val name: String = "wheelTimerLooper",
  val taskQueueFactory: () -> TaskQueue = { NonLockTaskQueue() }
) : Timer {
  var closed = false
  val taskQueueArray = AtomicReferenceArray(Array(wheelSize) { taskQueueFactory() })
  private var position = 0

  override fun exec(timeout: Long, task: () -> Unit): TimerTask {
    //val index = ((timeout / tick + position + if (timeout % tick == 0L) 0 else 1) % wheelSize).toInt()
    val index = ((timeout / tick + position) % wheelSize).toInt()
    return taskQueueArray[index].offer(task, timeout)
  }

  init {
    thread(isDaemon = true, name = name) {
      val startTime = System.currentTimeMillis()
      while (!closed) {
        position %= wheelSize

        val newQueue = taskQueueFactory()
        val taskQueue = taskQueueArray.getAndSet(position++, newQueue)

        //val time = System.currentTimeMillis()
        var node = taskQueue.take()
        while (node != null) {
          if (!node.canceled && node.isOutTime) {
            val sNode = node
            runNow { sNode.task() }
          } else {
            newQueue.offer(node)
          }
          node = taskQueue.take()
        }

        val nextSleep = startTime + tick * position - System.currentTimeMillis()
        if (nextSleep > 0) sleep(tick)
      }
    }
  }

  companion object {
    val timer by lazy { WheelTimer(200, 1024) }
    val smoothTimer by lazy { WheelTimer(20, 128) }
  }
}