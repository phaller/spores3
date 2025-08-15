package spores.pickle.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import upickle.default.*

import spores.{Spore, Reflection, SporeBuilder}
import spores.default.given


@RunWith(classOf[JUnit4])
class PickleTests {

  @Test
  def testReflection(): Unit = {
    val b = Reflection.loadModuleFieldValue[SporeBuilder[Int => Int => Int]]("spores.pickle.test.MySpore$")
    val fun = b.fun
    val res = fun(12)(3)
    assert(res == 16)
  }

  @Test
  def testSporeReadWriter(): Unit = {
    // create a spore
    val spore: Spore[Int => Int] = MySpore.build().withEnv(12)

    // pickle spore
    val pickled = write(spore)
    assert(pickled == """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedObject","funName":"spores.pickle.test.MySpore$"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"12","rw":{"$type":"spores.Packed.PackedObject","funName":"spores.ReadWriters$IntRW$"}}}""")

    // unpickle spore
    val unpickled = read[Spore[Int => Int]](pickled)
    assert(unpickled.unwrap()(3) == 16)
    assert(unpickled == spore)
  }

  @Test
  def testSporeWithoutEnvReadWriter(): Unit = {
    val spore: Spore[Int => Int] = SporeWithoutEnv.build()

    val pickled = write(spore)
    assert(pickled == """{"$type":"spores.Packed.PackedObject","funName":"spores.pickle.test.SporeWithoutEnv$"}""")

    val unpickled = read[Spore[Int => Int]](pickled)
    assert(unpickled.unwrap()(3) == 4)
    assert(unpickled == spore)
  }

  @Test
  def testSporeAppendStringReadWriter(): Unit = {
    val spore: Spore[List[String] => List[String]] = AppendString.build().withEnv("three")

    val pickled = write(spore)
    assert(pickled == """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedObject","funName":"spores.pickle.test.AppendString$"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"\"three\"","rw":{"$type":"spores.Packed.PackedObject","funName":"spores.ReadWriters$StringRW$"}}}""")

    val unpickled = read[Spore[List[String] => List[String]]](pickled)
    val l3 = List("four")
    assert(unpickled.unwrap()(l3) == List("four", "three"))
    assert(unpickled == spore)
  }

}
