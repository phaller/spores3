package spores.pickle.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import spores.{Spore, Creator, SporeData, PackedSporeData}
import spores.upickle.given

import upickle.default.*


@RunWith(classOf[JUnit4])
class PickleTests {

  @Test
  def testCreator(): Unit = {
    val c = Creator[Int, Int, Int]("spores.pickle.test.MySpore")
    val s = c(12)
    val res = s(3)
    assert(res == 16)
  }

  type SporeT = Spore[Int, Int] { type Env = Int }

  @Test
  def testSporeReadWriter(): Unit = {
    val name = "com.phaller.spores.pickle.test.MySpore"
    val env: Int = 12

    given sporeReadWriter: ReadWriter[SporeT] =
      readwriter[ujson.Value].bimap[SporeT](
        // currently, cannot obtain creator name from spore `b`
        b => ujson.Arr(name, b.envir),
        json => {
          val n: String = json(0).str
          val i: Int = json(1).num.toInt
          val c = Creator[Int, Int, Int](n)
          c(i)
        }
      )

    // create a spore
    val spore: SporeT = MySpore(12)

    // pickle spore
    val res = write(spore)

    assert(res == """["com.phaller.spores.pickle.test.MySpore",12]""")
  }

  @Test
  def testSporeDataReadWriter(): Unit = {
    val x = 12 // environment
    val data = SporeData(MySpore, Some(x))
    // pickle spore data
    val pickledData = write(data)
    assert(pickledData == """["spores.pickle.test.MySpore",1,"12"]""")
    val unpickledData = read[SporeData[Int, Int] { type Env = Int }](pickledData)
    assert(unpickledData.fqn == data.fqn)
    assert(unpickledData.envOpt == data.envOpt)
  }

  @Test
  def testSporeDataToBlock(): Unit = {
    val x = 12 // environment
    val data = SporeData(MySpore, Some(x))
    val spore = data.toSpore
    assert(spore(3) == 16)
  }

  @Test
  def testSporeDataToBlockWithoutEnv(): Unit = {
    val data = SporeData(SporeWithoutEnv)
    val spore = data.toSpore
    assert(spore(3) == 4)
  }

  @Test
  def testSporeDataReadWriterToBlock(): Unit = {
    val x = 12 // environment
    val data = SporeData(MySpore, Some(x))
    // pickle spore data
    val pickledData = write(data)
    assert(pickledData == """["spores.pickle.test.MySpore",1,"12"]""")
    val unpickledData = read[SporeData[Int, Int] { type Env = Int }](pickledData)
    val unpickledSpore = unpickledData.toSpore
    assert(unpickledSpore(3) == 16)
  }

  @Test
  def testSporeDataReadWriterToBlockWithoutEnv(): Unit = {
    val data = SporeData(SporeWithoutEnv)
    // pickle spore data
    val pickledData = write(data)
    assert(pickledData == """["spores.pickle.test.SporeWithoutEnv",0]""")
    val unpickledData = read[SporeData[Int, Int] { type Env = Nothing }](pickledData)
    val unpickledSpore = unpickledData.toSpore
    assert(unpickledSpore(3) == 4)
  }

  @Test
  def testPackedSporeDataReadWriterToSpore(): Unit = {
    val x = 12 // environment
    val data = SporeData(MySpore, Some(x))
    // pickle spore data
    val pickledData = write(data)
    assert(pickledData == """["spores.pickle.test.MySpore",1,"12"]""")
    val unpickledData = read[PackedSporeData](pickledData)
    val unpickledSpore = unpickledData.toSpore[Int, Int]
    assert(unpickledSpore(3) == 16)
  }

  @Test
  def testPackedSporeDataReadWriterToBlockWithoutEnv(): Unit = {
    val data = SporeData(SporeWithoutEnv)
    // pickle spore data
    val pickledData = write(data)
    assert(pickledData == """["spores.pickle.test.SporeWithoutEnv",0]""")
    val unpickledData = read[PackedSporeData](pickledData)
    val unpickledSpore = unpickledData.toSpore[Int, Int]
    assert(unpickledSpore(3) == 4)
  }

  @Test
  def testPickleAppendString(): Unit = {
    val appendData = SporeData(AppendString, Some("three"))
    val serialized = write(appendData)
    assert(serialized == """["spores.pickle.test.AppendString",1,"\"three\""]""")
    val deserData = read[PackedSporeData](serialized)
    val deserSpore = deserData.toSpore[List[String], List[String]]
    val l3 = List("four")
    assert(deserSpore(l3) == List("four", "three"))
  }

}
