package cn.tursom.utils

import java.lang.Thread.sleep
import java.util.concurrent.TimeUnit
import java.util.concurrent.ScheduledThreadPoolExecutor


object CurrentTimeMillisClock {
  @Volatile
  private var tick: Long = System.currentTimeMillis()

  val now get() = tick

  init {
    ScheduledThreadPoolExecutor(1) { runnable ->
      val thread = Thread(runnable, "current-time-millis")
      thread.isDaemon = true
      thread
    }.scheduleAtFixedRate({ tick = System.currentTimeMillis() }, 1, 1, TimeUnit.MILLISECONDS)
  }
}