package spores
package test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import spores.Spore.thunk
import spores.Duplicable.duplicate


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
    val b = thunk(x) { env =>
      env + 1
    }

    val b2 = duplicate(b)

    val res = b2()
    assert(res == 6)

    assert(b.envir == b2.envir)
  }

  @Test
  def testDuplicateThunkWithMutableClass(): Unit = {
    val x = new C
    x.f = 4

    val b = thunk(x) { env =>
      env.f + 1
    }

    val b2 = duplicate(b)

    val res = b2()
    assert(res == 5)

    assert(b.envir ne b2.envir)
    assert(b.envir.f == b2.envir.f)
  }

  @Test
  def testDuplicatedThunkAccessesNewEnv(): Unit = {
    val x = new C

    val b = thunk(x) { env =>
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

    val b: Spore[Unit, C] { type Env = C } = thunk(x) { env =>
      env
    }

    val b2 = duplicate(b)

    val envVal = b2(())

    assert(envVal.f == 7)
    assert(envVal ne x)
  }

  @Test
  def testDuplicatedThunk2(): Unit = {
    val x = new C
    x.f = 7

    val b: Spore[Unit, C] { type Env = C } = thunk(x) { env =>
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
    val s = Spore {
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

    val b = Spore(x) {
      env => (y: Int) => env.f + y
    }

    val b2 = duplicate(b)
    val res = b2(3)
    assert(res == 7)
  }

  @Test
  def testDuplicateSporeWithEnvGeneric(): Unit = {
    def duplicateThenApply[T, R, B <: Spore[T, R] : Duplicable](spore: B, arg: T): R = {
      val dup = summon[Duplicable[B]]
      val duplicated = dup.duplicate(spore)
      duplicated(arg)
    }

    val x = new C
    x.f = 4

    val b = Spore(x) {
      env => (y: Int) => env.f + y
    }

    val res = duplicateThenApply(b, 3)
    assert(res == 7)
  }

  @Test
  def testPassingSpore(): Unit = {
    def m2(s: Spore[Int, Int], arg: Int): Int = {
      s(arg)
    }

    def m1(s: Spore[Int, Int]): Int = {
      m2(s, 10) + 20
    }

    val x = new C
    x.f = 4

    val s = Spore(x) {
      env => (y: Int) => env.f + y
    }

    val res = m1(s)
    assert(res == 34)
  }

  @Test
  def testPassingSporeAndDuplicate(): Unit = {
    def m2[B <: Spore[Int, Int] : Duplicable](spore: B, arg: Int): Int = {
      val dup = summon[Duplicable[B]]
      val duplicated = dup.duplicate(spore)
      duplicated(arg)
    }

    def m1[B <: Spore[Int, Int] : Duplicable](spore: B): Int = {
      m2(spore, 10) + 20
    }

    val x = new C
    x.f = 4

    val b = Spore(x) {
      env => (y: Int) => env.f + y
    }

    val res = m1(b)
    assert(res == 34)
  }

  @Test
  def testPassingSporeAndDuplicateHelperClass(): Unit = {
    def m2[E](d: DSpore[Int, Int], arg: Int): Int = {
      val duplicated = d.duplicate()
      duplicated(arg)
    }

    def m1(s: DSpore[Int, Int]): Int = {
      m2(s, 10) + 20
    }

    val x = new C
    x.f = 4

    val s = Spore(x) {
      env => (y: Int) => env.f + y
    }

    val res = m1(DSpore(s))
    assert(res == 34)
  }

  @Test
  def testDuplicateThenApply(): Unit = {
    def duplicateThenApply[S <: Spore[Unit, C] : Duplicable](s: S): C = {
      val dup = summon[Duplicable[S]]
      val duplicated = dup.duplicate(s)
      duplicated()
    }

    val x = new C
    // x is a mutable instance:
    x.f = 7

    // create thunk:
    val b = thunk(x) { env =>
      env
    }

    val y = duplicateThenApply(b)
    assert(y.f == 7)
    // references are not equal:
    assert(y ne x)
  }

  @Test
  def testDuplicateDSpore(): Unit = {
    val x = new C
    // x is a mutable instance:
    x.f = 7

    val db = DSpore(Spore(x) {
      env => (y: Int) => env
    })

    val dspore2 = db.duplicate()

    val res2 = dspore2(5)

    assert(dspore2 ne db)
    assert(res2.f == x.f)
    assert(res2 ne x)
  }

  @Test
  def testDuplicateDSporeWithoutEnv(): Unit = {
    val db = DSpore(Spore {
      (y: Int) => y + 1
    })

    val dspore2 = db.duplicate()

    val res2 = dspore2(5)

    assert(dspore2 ne db)
    assert(res2 == db.spore(5))
  }

  @Test
  def testDuplicateDSporeAsParam(): Unit = {

    def fun(num: Int, body: DSpore[Int, C]): C = {
      val duplicatedBody = body.duplicate()
      duplicatedBody(num)
    }

    val x = new C
    // x is a mutable instance:
    x.f = 7

    val ds = DSpore(Spore(x) {
      env => (y: Int) => env
    })

    val res = fun(5, ds)

    assert(res.f == x.f)
    assert(res ne x)
  }

}
