package com.phaller.blocks.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.phaller.blocks.{Block, DBlock}
import Block.{env, thunk}


@RunWith(classOf[JUnit4])
class DBlockTests {

  @Test
  def testDuplicateThunk(): Unit = {
    val x = 5
    val b = thunk(x) {
      env + 1
    }

    val db = DBlock(b)
    val b2 = db.duplicable.duplicate(db.block)

    val res = b2()
    assert(res == 6)

    // comparing environments does not type-check:
    //assert(t.envir == b2.envir)
  }

  @Test
  def testDuplicatedThunkAccessesNewEnv(): Unit = {
    val x = new C

    val b = thunk(x) {
      env
    }

    val db = DBlock(b)
    val b2 = db.duplicable.duplicate(db.block)

    val envVal = b2()

    assert(envVal ne x)
  }

  @Test
  def testDuplicateBlockWithoutEnv(): Unit = {
    // block does not capture anything
    val b = Block {
      (x: Int) => x + 2
    }
    val db = DBlock(b)
    val b2 = db.duplicable.duplicate(db.block)
    val res = b2(3)
    assert(res == 5)
  }

  @Test
  def testDuplicateBlockWithEnv(): Unit = {
    val x = new C
    x.f = 4

    val b = Block(x) {
      (y: Int) => env.f + y
    }

    val db = DBlock(b)
    val b2 = db.duplicable.duplicate(db.block)
    val res = b2(3)
    assert(res == 7)
  }

}
