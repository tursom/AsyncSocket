package cn.tursom

inline fun usingTime(action: () -> Unit): Long {
  val t1 = System.currentTimeMillis()
  action()
  val t2 = System.currentTimeMillis()
  return t2 - t1
}

fun main() {
  val test = 5000L
  val repeat = 2000000000
  val repeat2 = 200
  var testN: Int
  println(usingTime {
    repeat(repeat2) {
      repeat(repeat) {
        testN = (test * .005f).toInt()
      }
    }
  })
  println(usingTime {
    repeat(repeat2) {
      repeat(repeat) {
        testN = (test / 200).toInt()
      }
    }
  })
  //println(testN)
}