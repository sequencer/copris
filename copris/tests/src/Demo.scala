package copris.tests

import utest._

object CoprisTest extends TestSuite {
  val tests = Tests {
    test("test1") {
      import copris._
      import copris.dsl._
      val x = int('x, 0, 7)
      val y = int('y, 0, 7)
      add(x + y === 7)
      if(find){
        println(solution)
      }
    }
  }
}
