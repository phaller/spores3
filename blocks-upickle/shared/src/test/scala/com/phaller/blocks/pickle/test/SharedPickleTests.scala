package com.phaller.blocks.pickle.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.phaller.blocks.Block
import com.phaller.blocks.Creator

import upickle.default._


@RunWith(classOf[JUnit4])
class SharedPickleTests {

  @Test
  def test1(): Unit = {
    val res = write(Seq(1, 2, 3))
    assert(res == "[1,2,3]")
  }

  @Test
  def testCreator(): Unit = {
    val c = Creator[Int, Int, Int]("com.phaller.blocks.pickle.test.MyBlock")
    val s = c(12)
    val res = s(3)
    assert(res == 16)
  }

  type BlockT = Block[Int, Int] { type Env = Int }

  @Test
  def testBlockReadWriter(): Unit = {
    val name = "com.phaller.blocks.pickle.test.MyBlock"
    val env: Int = 12

    given blockReadWriter: ReadWriter[BlockT] =
      readwriter[ujson.Value].bimap[BlockT](
        // currently, cannot obtain creator name from block `b`
        b => ujson.Arr(name, b.envir),
        json => {
          val n: String = json(0).str
          val i: Int = json(1).num.toInt
          val c = Creator[Int, Int, Int](n)
          c(i)
        }
      )

    // create a block
    val block: BlockT = MyBlock(12)

    // pickle block
    val res = write(block)

    assert(res == """["com.phaller.blocks.pickle.test.MyBlock",12]""")
  }

}
