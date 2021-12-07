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

}
