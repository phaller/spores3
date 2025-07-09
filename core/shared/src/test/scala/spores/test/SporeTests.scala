package spores.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import spores.Spore
import spores.Spore.thunk


@RunWith(classOf[JUnit4])
class SporeTests {

  @Test
  def testWithoutEnv(): Unit = {
    val b = Spore { (x: Int) => x + 2 }
    val res = b(3)
    assert(res == 5)
  }

  @Test
  def testWithoutEnv2(): Unit = {
    def fun(s: Spore[Int, Int] { type Env = Nothing }): Unit = {}

    val s = Spore((x: Int) => x + 2)

    fun(s)

    val res = s(3)
    assert(res == 5)
  }

  @Test
  def testWithoutEnvWithType(): Unit = {
    val s: Spore[Int, Int] { type Env = Nothing } = Spore {
      (x: Int) => x + 2
    }
    val res = s(3)
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
  /*@Test
  def testWithoutEnvWithType1(): Unit = {
    val y = 5
    val s: Spore[Int, Int] { type Env = Nothing } = Spore(y) {
      (x: Int) => x + 2 + env
    }
    val res = s(3)
    assert(res == 5)
  }*/

  @Test
  def testWithEnv(): Unit = {
    val y = 5
    val s = Spore(y) {
      env => (x: Int) => x + env
    }
    val res = s(10)
    assert(res == 15)
  }

  /*
[error] -- Error: [...]/BlockTests.scala:83:35
[error] 83 |      env => (x: Int) => x + env + z
[error]    |                                   ^
[error]    |Invalid capture of variable `z`. Use first parameter of spore's body to refer to the spore's environment.
   */
  /*@Test
  def testWithEnvInvalidCapture(): Unit = {
    val y = 5
    val z = 6
    val s = Spore(y) {
      env => (x: Int) => x + env + z
    }
    val res = s(10)
    assert(res == 21)
  }*/

  @Test
  def testWithEnv2(): Unit = {
    val str = "anonymous function"
    val s: Spore[Int, Int] { type Env = String } = Spore(str) {
      env => (x: Int) => x + env.length
    }
    val res = s(10)
    assert(res == 28)
  }

  @Test
  def testWithEnvTuple(): Unit = {
    val str = "anonymous function"
    val i = 5

    val s: Spore[Int, Int] { type Env = (String, Int) } = Spore((str, i)) {
      case (l, r) => (x: Int) => x + l.length - r
    }

    val res = s(10)
    assert(res == 23)
  }

  @Test
  def testWithEnvParamUntupling(): Unit = {
    val str = "anonymous function"
    val i = 5

    val s = Spore((str, i)) {
      (l, r) => (x: Int) => x + l.length - r
    }

    val res = s(10)
    assert(res == 23)
  }

  @Test
  def testWithEnvWithType(): Unit = {
    val y = 5
    val s: Spore[Int, Int] { type Env = Int } = Spore(y) {
      env => (x: Int) => x + env
    }
    val res = s(11)
    assert(res == 16)
  }

  @Test
  def testThunk(): Unit = {
    val x = 5
    val t = thunk(x) { env =>
      env + 7
    }
    val res = t()
    assert(res == 12)
  }

  @Test
  def testNestedWithoutEnv(): Unit = {
    val s = Spore {
      (x: Int) =>
        val s2 = Spore { (y: Int) => y - 1 }
        s2(x) + 2
    }
    val res = s(3)
    assert(res == 4)
  }

  @Test
  def testNestedWithEnv1(): Unit = {
    val z = 5

    val s = Spore(z) {
      env => (x: Int) =>
        val s2 = Spore(env) { env => (y: Int) => env + y - 1 }
        s2(x) + 2
    }
    val res = s(3)
    assert(res == 9)
  }

  @Test
  def testNestedWithEnv2(): Unit = {
    val z = 5
    val w = 6

    val s = Spore((w, z)) {
      case (l, r) => (x: Int) =>
        val s2 = Spore(r) { env => (y: Int) => env + y - 1 }
        s2(x) + 2 - l
    }

    val res = s(3)
    assert(res == 3)
  }

  @Test
  def testLocalClasses(): Unit = {
    val x = 5

    val s = Spore(x) { env => (y: Int) =>
      class Local2 { def m() = y }
      class Local(p: Int)(using loc: Local2) {
        val fld = env + p
      }

      given l2: Local2 = new Local2
      val l = new Local(y + 1)
      l.fld
    }

    val res = s(3)
    assert(res == 9)
  }

  @Test
  def testThreadSafe(): Unit = {
    given ThreadSafe[Int] = new ThreadSafe[Int] {}

    def fun(s: Spore[Int, Int], x: Int)(using ThreadSafe[s.Env]): Int =
      s(x)

    val y = 5
    val s = Spore(y) {
      env => (x: Int) => x + env
    }

    val res = fun(s, 10)
    assert(res == 15)
  }

}

trait ThreadSafe[T]
