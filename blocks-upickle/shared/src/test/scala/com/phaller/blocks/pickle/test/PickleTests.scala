package com.phaller.blocks.pickle.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.phaller.blocks.{Block, Creator, BlockData, PackedBlockData}
import com.phaller.blocks.pickle.given

import upickle.default.*


@RunWith(classOf[JUnit4])
class PickleTests {

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

  @Test
  def testBlockDataReadWriter(): Unit = {
    val x = 12 // environment
    val data = BlockData(MyBlock, Some(x))
    // pickle block data
    val pickledData = write(data)
    assert(pickledData == """["com.phaller.blocks.pickle.test.MyBlock",1,"12"]""")
    val unpickledData = read[BlockData[Int, Int] { type Env = Int }](pickledData)
    assert(unpickledData.fqn == data.fqn)
    assert(unpickledData.envOpt == data.envOpt)
  }

  @Test
  def testBlockDataToBlock(): Unit = {
    val x = 12 // environment
    val data = BlockData(MyBlock, Some(x))
    val block = data.toBlock
    assert(block(3) == 16)
  }

  @Test
  def testBlockDataToBlockWithoutEnv(): Unit = {
    val data = BlockData(BlockWithoutEnv, None)
    val block = data.toBlock
    assert(block(3) == 4)
  }

  @Test
  def testBlockDataReadWriterToBlock(): Unit = {
    val x = 12 // environment
    val data = BlockData(MyBlock, Some(x))
    // pickle block data
    val pickledData = write(data)
    assert(pickledData == """["com.phaller.blocks.pickle.test.MyBlock",1,"12"]""")
    val unpickledData = read[BlockData[Int, Int] { type Env = Int }](pickledData)
    val unpickledBlock = unpickledData.toBlock
    assert(unpickledBlock(3) == 16)
  }

  @Test
  def testBlockDataReadWriterToBlockWithoutEnv(): Unit = {
    val data = BlockData(BlockWithoutEnv, None)
    // pickle block data
    val pickledData = write(data)
    assert(pickledData == """["com.phaller.blocks.pickle.test.BlockWithoutEnv",0]""")
    val unpickledData = read[BlockData[Int, Int] { type Env = Nothing }](pickledData)
    val unpickledBlock = unpickledData.toBlock
    assert(unpickledBlock(3) == 4)
  }

  @Test
  def testPackedBlockDataReadWriterToBlock(): Unit = {
    val x = 12 // environment
    val data = BlockData(MyBlock, Some(x))
    // pickle block data
    val pickledData = write(data)
    assert(pickledData == """["com.phaller.blocks.pickle.test.MyBlock",1,"12"]""")
    val unpickledData = read[PackedBlockData](pickledData)
    val unpickledBlock = unpickledData.toBlock[Int, Int]
    assert(unpickledBlock(3) == 16)
  }

  @Test
  def testPackedBlockDataReadWriterToBlockWithoutEnv(): Unit = {
    val data = BlockData(BlockWithoutEnv, None)
    // pickle block data
    val pickledData = write(data)
    assert(pickledData == """["com.phaller.blocks.pickle.test.BlockWithoutEnv",0]""")
    val unpickledData = read[PackedBlockData](pickledData)
    val unpickledBlock = unpickledData.toBlock[Int, Int]
    assert(unpickledBlock(3) == 4)
  }

  @Test
  def testPickleAppendString(): Unit = {
    val appendData = BlockData(AppendString, Some("three"))
    val serialized = write(appendData)
    assert(serialized == """["com.phaller.blocks.pickle.test.AppendString",1,"\"three\""]""")
    val deserData = read[PackedBlockData](serialized)
    val deserBlock = deserData.toBlock[List[String], List[String]]
    val l3 = List("four")
    assert(deserBlock(l3) == List("four", "three"))
  }

}
