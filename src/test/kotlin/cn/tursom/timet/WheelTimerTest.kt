package cn.tursom.timet

import cn.tursom.timer.WheelTimer
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WheelTimerTest {
  private val timer = WheelTimer.timer
  @Test
  fun testTimer() {
    runBlocking {
      println(System.currentTimeMillis())
      println(suspendCoroutine<String> { cont ->
        timer.exec(10) {
          cont.resume("Hello")
        }
      })
      println(System.currentTimeMillis())
    }
  }
}