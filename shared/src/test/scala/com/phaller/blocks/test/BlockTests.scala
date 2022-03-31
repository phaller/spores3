package com.phaller
package blocks.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import blocks.Block
import blocks.Block.{env, thunk}


@RunWith(classOf[JUnit4])
class BlockTests {

  @Test
  def testWithoutEnv(): Unit = {
    val s = Block {
      (x: Int) => x + 2
    }
    val res = s(3)
    assert(res == 5)
  }

  @Test
  def testWithoutEnvWithType(): Unit = {
    val s: Block[Int, Int] { type Env = Nothing } = Block {
      (x: Int) => x + 2
    }
    val res = s(3)
    assert(res == 5)
  }

  /* the following does not compile:
[error] -- [E007] Type Mismatch Error: [...]/BlockTests.scala:37:61 
[error] 37 |    val s: Block[Int, Int] { type Env = Nothing } = Block(y) {
[error]    |                                                    ^
[error]    |             Found:    com.phaller.blocks.Block[Int, Int]{Env = Int}
[error]    |             Required: com.phaller.blocks.Block[Int, Int]{Env = Nothing}
[error] 38 |      (x: Int) => x + 2 + env
[error] 39 |    }
   */
  /*@Test
  def testWithoutEnvWithType1(): Unit = {
    val y = 5
    val s: Block[Int, Int] { type Env = Nothing } = Block(y) {
      (x: Int) => x + 2 + env
    }
    val res = s(3)
    assert(res == 5)
  }*/

  @Test
  def testWithEnv(): Unit = {
    val y = 5
    val s = Block(y) {
      (x: Int) => x + env
    }
    val res = s(10)
    assert(res == 15)
  }

  @Test
  def testWithEnvWithType(): Unit = {
    val y = 5
    val s: Block[Int, Int] { type Env = Int } = Block(y) {
      (x: Int) => x + env
    }
    val res = s(11)
    assert(res == 16)
  }

  @Test
  def testThunk(): Unit = {
    val x = 5
    val t = thunk(x) {
      env + 7
    }
    val res = t()
    assert(res == 12)
  }

  @Test
  def testNestedWithoutEnv(): Unit = {
    val s = Block {
      (x: Int) =>
        val s2 = Block { (y: Int) => y - 1 }
        s2(x) + 2
    }
    val res = s(3)
    assert(res == 4)
  }

  @Test
  def testNestedWithEnv1(): Unit = {
    val z = 5

    val s = Block {
      (x: Int) =>
        val s2 = Block(z) { (y: Int) => env + y - 1 }
        s2(x) + 2
    }
    val res = s(3)
    assert(res == 9)
  }

  @Test
  def testNestedWithEnv2(): Unit = {
    val z = 5
    val w = 6

    val s = Block(w) {
      (x: Int) =>
        val s2 = Block(z) { (y: Int) => env + y - 1 }
        s2(x) + 2 - env
    }

    val res = s(3)
    assert(res == 3)
  }

  @Test
  def testThreadSafe(): Unit = {
    given ThreadSafe[Int] = new ThreadSafe[Int] {}

    def fun(b: Block[Int, Int], x: Int)(using ThreadSafe[b.Env]): Int =
      b(x)

    val y = 5
    val s = Block(y) {
      (x: Int) => x + env
    }

    val res = fun(s, 10)
    assert(res == 15)
  }

}

trait ThreadSafe[T]
