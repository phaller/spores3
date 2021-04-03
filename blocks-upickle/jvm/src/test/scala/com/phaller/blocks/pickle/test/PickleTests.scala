package com.phaller.blocks.pickle.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.phaller.blocks.{Block, Creator}

import upickle.default._


object MyBlock extends Block.Creator[Int, Int, Int](
  (x: Int) => Block.env + x + 1
)

@RunWith(classOf[JUnit4])
class PickleTests {

  @Test
  def testCreator(): Unit = {
    val c = Creator[Int, Int, Int]("com.phaller.blocks.pickle.test.MyBlock")
    val s = c(12)
    val res = s(3)
    assert(res == 16)
  }

  @Test
  def testUpickle(): Unit = {
    val res = write(Seq(1, 2, 3))
    assert(res == "[1,2,3]")
  }

}
