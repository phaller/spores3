package com.phaller
package blocks
package test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import blocks.Block.{thunk, env}


private class C {
  var f: Int = 0
}

@RunWith(classOf[JUnit4])
class DuplicableTests {

  def dup[T: Duplicable](x: T): T =
    summon[Duplicable[T]].duplicate(x)

  given Duplicable[C] with {
    def duplicate(x: C): C = {
      val y = new C
      y.f = x.f
      y
    }
  }

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

    val b: Thunk[C] { type Env = C } = thunk(x) {
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
  def testDuplicateThenApply(): Unit = {
    def duplicateThenApply[S <: Thunk[C] : Duplicable](s: S): C = {
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

    val block = Block(x) {
      (y: Int) => env
    }

    val dblock = Block.makeDBlock(block)

    val dblock2 = dblock.duplicate()

    val res2 = dblock2(5)

    assert(dblock2 ne dblock)
    assert(res2.f == x.f)
    assert(res2 ne x)
  }

  @Test
  def testDuplicateDBlockWithoutEnv(): Unit = {
    val block = Block {
      (y: Int) => y + 1
    }

    val dblock = Block.makeDBlock(block)

    val dblock2 = dblock.duplicate()

    val res2 = dblock2(5)

    assert(dblock2 ne dblock)
    assert(res2 == dblock(5))
  }

}
