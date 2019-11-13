package cn.tursom.timet

import cn.tursom.timer.WheelTimer
import cn.tursom.utils.CurrentTimeMillisClock
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class WheelTimerTest {
  private val timer = WheelTimer.timer
  @Test
  fun testTimer() {
    runBlocking {
      println(CurrentTimeMillisClock.now)
      println(suspendCoroutine<String> { cont ->
        timer.exec(10) {
          cont.resume("Hello")
        }
      })
      println(CurrentTimeMillisClock.now)
    }
  }
}