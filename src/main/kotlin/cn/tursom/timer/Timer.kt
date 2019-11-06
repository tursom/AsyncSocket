package cn.tursom.timer

interface Timer {
	fun exec(timeout: Long, task: () -> Unit): TimerTask
}
