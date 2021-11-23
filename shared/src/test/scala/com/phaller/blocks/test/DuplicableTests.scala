package com.phaller
package blocks
package test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import blocks.Block.{thunk, env}


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

  def dup[T: Duplicable](x: T): T =
    summon[Duplicable[T]].duplicate(x)

  @Test
  def testDuplicateInt(): Unit = {
    val x = 5
    val dup = summon[Duplicable[Int]]
    assert(5 == dup.duplicate(x))
  }

  @Test
  def testDuplicateThunk(): Unit = {
    val x = 5
    val b = thunk(x) {
      env + 1
    }

    val b2 = dup(b)

    val res = b2()
    assert(res == 6)

    assert(b.envir == b2.envir)
  }

  @Test
  def testDuplicateThunkWithMutableClass(): Unit = {
    val x = new C
    x.f = 4

    val b = thunk(x) {
      env.f + 1
    }

    val b2 = dup(b)

    val res = b2()
    assert(res == 5)

    assert(b.envir ne b2.envir)
    assert(b.envir.f == b2.envir.f)
  }

  @Test
  def testDuplicatedThunkAccessesNewEnv(): Unit = {
    val x = new C

    val b = thunk(x) {
      env
    }

    val b2 = dup(b)

    val envVal = b2()

    assert(envVal ne x)
  }

  @Test
  def testDuplicatedThunk1(): Unit = {
    val x = new C
    x.f = 7

    val b: Block[Unit, C] { type Env = C } = thunk(x) {
      env
    }

    val b2 = dup(b)

    val envVal = b2(())

    assert(envVal.f == 7)
    assert(envVal ne x)
  }

  @Test
  def testDuplicatedThunk2(): Unit = {
    val x = new C
    x.f = 7

    val b: Block[Unit, C] { type Env = C } = thunk(x) {
      env
    }

    val b2 = dup(b)

    val envVal = b2()

    assert(envVal.f == 7)
    assert(envVal ne x)
  }

  @Test
  def testDuplicatedBlockNoCapture(): Unit = {
    // block does not capture anything
    val s = Block {
      (x: Int) => x + 2
    }
    val s2 = dup(s)
    val res = s2(3)
    assert(res == 5)
  }

  @Test
  def testDuplicateBlockWithEnv(): Unit = {
    val x = new C
    x.f = 4

    val b = Block(x) {
      (y: Int) => env.f + y
    }

    val b2 = dup(b)
    val res = b2(3)
    assert(res == 7)
  }

  @Test
  def testDuplicateBlockWithEnvGeneric(): Unit = {
    def duplicateThenApply[T, R, B <: Block[T, R] : Duplicable](block: B, arg: T): R = {
      val dup = summon[Duplicable[B]]
      val duplicated = dup.duplicate(block)
      duplicated(arg)
    }

    val x = new C
    x.f = 4

    val b = Block(x) {
      (y: Int) => env.f + y
    }

    val res = duplicateThenApply(b, 3)
    assert(res == 7)
  }

  @Test
  def testPassingBlock(): Unit = {
    def m2(block: Block[Int, Int], arg: Int): Int = {
      block(arg)
    }

    def m1(block: Block[Int, Int]): Int = {
      m2(block, 10) + 20
    }

    val x = new C
    x.f = 4

    val b = Block(x) {
      (y: Int) => env.f + y
    }

    val res = m1(b)
    assert(res == 34)
  }

  @Test
  def testPassingBlockAndDuplicate(): Unit = {
    def m2[B <: Block[Int, Int] : Duplicable](block: B, arg: Int): Int = {
      val dup = summon[Duplicable[B]]
      val duplicated = dup.duplicate(block)
      duplicated(arg)
    }

    def m1[B <: Block[Int, Int] : Duplicable](block: B): Int = {
      m2(block, 10) + 20
    }

    val x = new C
    x.f = 4

    val b = Block(x) {
      (y: Int) => env.f + y
    }

    val res = m1(b)
    assert(res == 34)
  }

  @Test
  def testPassingBlockAndDuplicateHelperClass(): Unit = {
    def m2[E](d: DBlock2[Int, Int], arg: Int): Int = {
      val duplicated = d.duplicate()
      duplicated(arg)
    }

    def m1(block: DBlock2[Int, Int]): Int = {
      m2(block, 10) + 20
    }

    val x = new C
    x.f = 4

    val b = Block(x) {
      (y: Int) => env.f + y
    }

    val res = m1(DBlock2(b))
    assert(res == 34)
  }

  @Test
  def testDuplicateThenApply(): Unit = {
    def duplicateThenApply[S <: Block[Unit, C] : Duplicable](s: S): C = {
      val dup = summon[Duplicable[S]]
      val duplicated = dup.duplicate(s)
      duplicated()
    }

    val x = new C
    // x is a mutable instance:
    x.f = 7

    // create thunk:
    val b = thunk(x) {
      env
    }

    val y = duplicateThenApply(b)
    assert(y.f == 7)
    // references are not equal:
    assert(y ne x)
  }

  @Test
  def testDuplicateDBlock(): Unit = {
    val x = new C
    // x is a mutable instance:
    x.f = 7

    val db = DBlock2(Block(x) {
      (y: Int) => env
    })

    val dblock2 = db.duplicate()

    val res2 = dblock2(5)

    assert(dblock2 ne db)
    assert(res2.f == x.f)
    assert(res2 ne x)
  }

  @Test
  def testDuplicateDBlockWithoutEnv(): Unit = {
    val db = DBlock2(Block {
      (y: Int) => y + 1
    })

    val dblock2 = db.duplicate()

    val res2 = dblock2(5)

    assert(dblock2 ne db)
    assert(res2 == db.block(5))
  }

  @Test
  def testDuplicateDBlockAsParam(): Unit = {

    def fun(num: Int, body: DBlock2[Int, C]): C = {
      val duplicatedBody = body.duplicate()
      duplicatedBody(num)
    }

    val x = new C
    // x is a mutable instance:
    x.f = 7

    val db = DBlock2(Block(x) {
      (y: Int) => env
    })

    val res = fun(5, db)

    assert(res.f == x.f)
    assert(res ne x)
  }

}
