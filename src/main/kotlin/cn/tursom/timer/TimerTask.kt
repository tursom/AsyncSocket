package cn.tursom.timer

interface TimerTask : () -> Unit {
  val canceled: Boolean
  val createTime: Long
  val timeout: Long
  val task: () -> Unit
  val outTime get() = createTime + timeout
  val isOutTime get() = System.currentTimeMillis() > outTime

  fun isOutTime(time: Long): Boolean = time > outTime
  fun cancel()
}