package spores
package test

import utest._

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

object DuplicableTests extends TestSuite {

  val tests = Tests {
    test("testDuplicateInt") {
      val x = 5
      val dup = summon[Duplicable[Int]]
      assert(5 == dup.duplicate(x))
    }

    test("testDuplicateThunk") {
      val x = 5
      val b = Duplicate.applyWithEnv(x) { env => () =>
        env + 1
      }

      val b2 = duplicate(b)

      val res = b2()
      assert(6 == res)
    }

    test("testDuplicateThunkWithMutableClass") {
      val x = new C
      x.f = 4

      val b = Duplicate.applyWithEnv(x) { env => () =>
        env.f + 1
      }

      val b2 = duplicate(b)

      val res = b2()
      assert(5 == res)

      val dup = b.asInstanceOf[DuplicateWithEnv[C, Int]]
      val dup2 = b2.asInstanceOf[DuplicateWithEnv[C, Int]]
      assert(dup.env.unwrap() ne dup2.env.unwrap())
      assert(dup.env.unwrap().f == dup2.env.unwrap().f)
    }

    test("testDuplicatedThunkAccessesNewEnv") {
      val x = new C

      val b = Duplicate.applyWithEnv(x) { env => () =>
        env
      }

      val b2 = duplicate(b)

      val envVal = b2()

      assert(envVal != x)
    }

    test("testDuplicatedThunk1") {
      val x = new C
      x.f = 7

      val b = Duplicate.applyWithEnv(x) { env => () =>
        env
      }

      val b2 = duplicate(b)

      val envVal = b2()

      assert(7 == envVal.f)
      assert(envVal ne x)
    }

    test("testDuplicatedThunk2") {
      val x = new C
      x.f = 7

      val b: Duplicate[() => C] = Duplicate.applyWithEnv(x) { env => () =>
        env
      }

      val b2 = duplicate(b)

      val envVal = b2()

      assert(7 == envVal.f)
      assert(envVal ne x)
    }

    test("testDuplicatedSporeNoCapture") {
      // spore does not capture anything
      val s = Duplicate {
        (x: Int) => x + 2
      }
      val s2 = duplicate(s)
      val res = s2(3)
      assert(5 == res)
    }

    test("testDuplicateSporeWithEnv") {
      val x = new C
      x.f = 4

      val b = Duplicate.applyWithEnv(x) {
        env => (y: Int) => env.f + y
      }

      val b2 = duplicate(b)
      val res = b2(3)
      assert(7 == res)
    }

    test("testDuplicateSporeWithEnvGeneric") {
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
      assert(7 == res)
    }

    test("testPassingSpore") {
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
      assert(34 == res)
    }

    test("testPassingSporeAndDuplicate") {
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
      assert(34 == res)
    }

    test("testDuplicateThenApply") {
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
      assert(7 == y.f)
      // references are not equal:
      assert(y ne x)
    }
  }

}
