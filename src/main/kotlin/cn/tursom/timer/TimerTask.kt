package cn.tursom.timer

interface TimerTask {
  val canceled: Boolean
  val createTime: Long
  val timeout: Long
  val task: () -> Unit
  val outTime get() = createTime + timeout
  val isOutTime get() = System.currentTimeMillis() > outTime

  fun isOutTime(time: Long): Boolean = time > outTime
  fun run() {
    if (!canceled) task()
  }

  fun cancel()
}