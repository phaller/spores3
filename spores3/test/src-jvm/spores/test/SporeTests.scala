package spores.test

import utest._

import spores.Spore
import spores.default.*
import spores.default.given


object SporeTests extends TestSuite {

  val tests = Tests {

    test("testWithoutEnv") {
      val b = Spore { (x: Int) => x + 2 }
      val res = b.get()(3)
      assert(res == 5)
    }

    test("testWithoutEnv2") {
      def fun(s: Spore[Int => Int]): Unit = {}

      val s = Spore((x: Int) => x + 2)

      fun(s)

      val res = s.get()(3)
      assert(res == 5)
    }

    test("testWithoutEnvWithType") {
      val s: Spore[Int => Int] = Spore {
        (x: Int) => x + 2
      }
      val res = s.get()(3)
      assert(res == 5)
    }

    test("testWithoutEnvWithType1") {
      val s: Spore[Int => Int] = Spore {
        x => x + 2
      }
      val res = s.get()(3)
      assert(res == 5)
    }

    /* the following does not compile:
  [error] -- [E007] Type Mismatch Error: [...]/BlockTests.scala:37:61
  [error] 37 |    val s: Spore[Int, Int] { type Env = Nothing } = Spore(y) {
  [error]    |                                                    ^
  [error]    |             Found:    com.phaller.blocks.Spore[Int, Int]{Env = Int}
  [error]    |             Required: com.phaller.blocks.Spore[Int, Int]{Env = Nothing}
  [error] 38 |      (x: Int) => x + 2 + env
  [error] 39 |    }
    */
    /*test("testWithoutEnvWithType1") {
      val y = 5
      val s: Spore[Int, Int] { type Env = Nothing } = Spore(y) {
        (x: Int) => x + 2 + env
      }
      val res = s(3)
      assert(res == 5)
    }*/

    test("testWithEnv") {
      val y = 5
      val s = Spore.applyWithEnv(y) {
        env => (x: Int) => x + env
      }
      val res = s.get()(10)
      assert(res == 15)
    }

    /*
  [error] -- Error: [...]/BlockTests.scala:83:35
  [error] 83 |      env => (x: Int) => x + env + z
  [error]    |                                   ^
  [error]    |Invalid capture of variable `z`. Use first parameter of spore's body to refer to the spore's environment.
    */
    /*test("testWithEnvInvalidCapture") {
      val y = 5
      val z = 6
      val s = Spore(y) {
        env => (x: Int) => x + env + z
      }
      val res = s(10)
      assert(res == 21)
    }*/

    test("testWithEnv2") {
      val str = "anonymous function"
      val s: Spore[Int => Int] = Spore.applyWithEnv(str) {
        env => (x: Int) => x + env.length
      }
      val res = s.get()(10)
      assert(res == 28)
    }

    test("testWithEnvTuple") {
      val str = "anonymous function"
      val i = 5

      val s: Spore[Int => Int] = Spore.applyWithEnv((str, i)) {
        case (l, r) => (x: Int) => x + l.length - r
      }

      val res = s.get()(10)
      assert(res == 23)
    }

    test("testWithEnvParamUntupling") {
      val str = "anonymous function"
      val i = 5

      val s = Spore.applyWithEnv((str, i)) {
        (l, r) => (x: Int) => x + l.length - r
      }

      val res = s.get()(10)
      assert(res == 23)
    }

    test("testWithEnvWithType") {
      val y = 5
      val s: Spore[Int => Int] = Spore.applyWithEnv(y) {
        env => (x: Int) => x + env
      }
      val res = s.get()(11)
      assert(res == 16)
    }

    test("testThunk") {
      val x = 5
      val t = Spore.applyWithEnv(x) { env => () =>
        env + 7
      }
      val res = t.get()()
      assert(res == 12)
    }

    test("testNestedWithoutEnv") {
      val s = Spore {
        (x: Int) =>
          val s2 = Spore { (y: Int) => y - 1 }
          s2.get()(x) + 2
      }
      val res = s.get()(3)
      assert(res == 4)
    }

    test("testNestedWithEnv1") {
      val z = 5

      val s = Spore.applyWithEnv(z) {
        env => (x: Int) =>
          val s2 = Spore.applyWithEnv(env) { env => (y: Int) => env + y - 1 }
          s2.get()(x) + 2
      }
      val res = s.get()(3)
      assert(res == 9)
    }

    test("testNestedWithEnv2") {
      val z = 5
      val w = 6

      val s = Spore.applyWithEnv((w, z)) {
        case (l, r) => (x: Int) =>
          val s2 = Spore.applyWithEnv(r) { env => (y: Int) => env + y - 1 }
          s2.get()(x) + 2 - l
      }

      val res = s.get()(3)
      assert(res == 3)
    }

    test("testLocalClasses") {
      val x = 5

      val s = Spore.applyWithEnv(x) { env => (y: Int) =>
        class Local2 { def m() = y }
        class Local(p: Int)(using loc: Local2) {
          val fld = env + p
        }

        given l2: Local2 = new Local2
        val l = new Local(y + 1)
        l.fld
      }

      val res = s.get()(3)
      assert(res == 9)
    }
  }

}
