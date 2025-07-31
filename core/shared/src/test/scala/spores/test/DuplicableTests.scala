package spores
package test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import spores.default.*
import spores.default.given


class C {
  var f: Int = 0
}

object C {
  given Duplicable[C] with {
    def duplicate(x: C): C = {
      val y = new C
      y.f = x.f
      y
    }
  }
}

@RunWith(classOf[JUnit4])
class DuplicableTests {

  @Test
  def testDuplicateInt(): Unit = {
    val x = 5
    val dup = summon[Duplicable[Int]]
    assert(5 == dup.duplicate(x))
  }

  @Test
  def testDuplicateThunk(): Unit = {
    val x = 5
    val b = Duplicate.applyWithEnv(x) { env => () =>
      env + 1
    }

    val b2 = duplicate(b)

    val res = b2()
    assert(res == 6)
  }

  @Test
  def testDuplicateThunkWithMutableClass(): Unit = {
    val x = new C
    x.f = 4

    val b = Duplicate.applyWithEnv(x) { env => () =>
      env.f + 1
    }

    val b2 = duplicate(b)

    val res = b2()
    assert(res == 5)

    val dup = b.asInstanceOf[DuplicateWithEnv[C, Int]]
    val dup2 = b2.asInstanceOf[DuplicateWithEnv[C, Int]]
    assert(dup.env.unwrap() ne dup2.env.unwrap())
    assert(dup.env.unwrap().f == dup2.env.unwrap().f)
  }

  @Test
  def testDuplicatedThunkAccessesNewEnv(): Unit = {
    val x = new C

    val b = Duplicate.applyWithEnv(x) { env => () =>
      env
    }

    val b2 = duplicate(b)

    val envVal = b2()

    assert(envVal != x)
  }

  @Test
  def testDuplicatedThunk1(): Unit = {
    val x = new C
    x.f = 7

    val b = Duplicate.applyWithEnv(x) { env => () =>
      env
    }

    val b2 = duplicate(b)

    val envVal = b2()

    assert(envVal.f == 7)
    assert(envVal ne x)
  }

  @Test
  def testDuplicatedThunk2(): Unit = {
    val x = new C
    x.f = 7

    val b: Duplicate[() => C] = Duplicate.applyWithEnv(x) { env => () =>
      env
    }

    val b2 = duplicate(b)

    val envVal = b2()

    assert(envVal.f == 7)
    assert(envVal ne x)
  }

  @Test
  def testDuplicatedSporeNoCapture(): Unit = {
    // spore does not capture anything
    val s = Duplicate {
      (x: Int) => x + 2
    }
    val s2 = duplicate(s)
    val res = s2(3)
    assert(res == 5)
  }

  @Test
  def testDuplicateSporeWithEnv(): Unit = {
    val x = new C
    x.f = 4

    val b = Duplicate.applyWithEnv(x) {
      env => (y: Int) => env.f + y
    }

    val b2 = duplicate(b)
    val res = b2(3)
    assert(res == 7)
  }

  @Test
  def testDuplicateSporeWithEnvGeneric(): Unit = {
    def duplicateThenApply[T, R, B <: Duplicate[T => R] : Duplicable](spore: B, arg: T): R = {
      val dup = summon[Duplicable[B]]
      val duplicated = dup.duplicate(spore)
      duplicated(arg)
    }

    val x = new C
    x.f = 4

    val b = Duplicate.applyWithEnv(x) {
      env => (y: Int) => env.f + y
    }

    val res = duplicateThenApply(b, 3)
    assert(res == 7)
  }

  @Test
  def testPassingSpore(): Unit = {
    def m2(s: Duplicate[Int => Int], arg: Int): Int = {
      s(arg)
    }

    def m1(s: Duplicate[Int => Int]): Int = {
      m2(s, 10) + 20
    }

    val x = new C
    x.f = 4

    val s = Duplicate.applyWithEnv(x) {
      env => (y: Int) => env.f + y
    }

    val res = m1(s)
    assert(res == 34)
  }

  @Test
  def testPassingSporeAndDuplicate(): Unit = {
    def m2[B <: Duplicate[Int => Int] : Duplicable](spore: B, arg: Int): Int = {
      val dup = summon[Duplicable[B]]
      val duplicated = dup.duplicate(spore)
      duplicated(arg)
    }

    def m1[B <: Duplicate[Int => Int] : Duplicable](spore: B): Int = {
      m2(spore, 10) + 20
    }

    val x = new C
    x.f = 4

    val b = Duplicate.applyWithEnv(x) {
      env => (y: Int) => env.f + y
    }

    val res = m1(b)
    assert(res == 34)
  }

  @Test
  def testDuplicateThenApply(): Unit = {
    def duplicateThenApply[S <: Duplicate[Unit => C] : Duplicable](s: S): C = {
      val dup = summon[Duplicable[S]]
      val duplicated = dup.duplicate(s)
      duplicated(())
    }

    val x = new C
    // x is a mutable instance:
    x.f = 7

    // create thunk:
    val b = Duplicate.applyWithEnv(x) { env => (_: Unit) =>
      env
    }

    val y = duplicateThenApply(b)
    assert(y.f == 7)
    // references are not equal:
    assert(y ne x)
  }

}
