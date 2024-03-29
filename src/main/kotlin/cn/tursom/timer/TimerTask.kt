package cn.tursom.timer

import cn.tursom.utils.CurrentTimeMillisClock

interface TimerTask : () -> Unit {
  val canceled: Boolean
  val createTime: Long
  val timeout: Long
  val task: () -> Unit
  val outTime get() = createTime + timeout
  val isOutTime get() = CurrentTimeMillisClock.now > outTime

  fun isOutTime(time: Long): Boolean = time > outTime
  fun cancel()
}