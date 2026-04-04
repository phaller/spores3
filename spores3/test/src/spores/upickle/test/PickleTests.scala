package spores.pickle.test

import utest._

import upickle.default.*

import spores.{Spore, Reflection, SporeBuilder}
import spores.default.given


object PickleTests extends TestSuite {

  val tests = Tests {
    test("testReflection") {
      val b = Reflection.loadModuleFieldValue[SporeBuilder[Int => Int => Int]]("spores.pickle.test.MySpore$")
      val fun = b.body
      val res = fun(12)(3)
      assert(16 == res)
    }

    test("testSporeReadWriter") {
      // create a spore
      val spore: Spore[Int => Int] = MySpore.build().withEnv(12)

      // pickle spore
      val pickled = write(spore)
      assert(pickled == """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.pickle.test.MySpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}""")

      // unpickle spore
      val unpickled = read[Spore[Int => Int]](pickled)
      assert(16 == unpickled.unwrap()(3))
      assert(unpickled == spore)
    }

    test("testSporeWithoutEnvReadWriter") {
      val spore: Spore[Int => Int] = SporeWithoutEnv.build()

      val pickled = write(spore)
      assert(pickled == """{"tag":"Body","kind":0,"className":"spores.pickle.test.SporeWithoutEnv$"}""")

      val unpickled = read[Spore[Int => Int]](pickled)
      assert(4 == unpickled.unwrap()(3))
      assert(unpickled == spore)
    }

    test("testSporeAppendStringReadWriter") {
      val spore: Spore[List[String] => List[String]] = AppendString.build().withEnv("three")

      val pickled = write(spore)
      assert(pickled == """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.pickle.test.AppendString$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$StringRW$"},"value":"three"}}""")

      val unpickled = read[Spore[List[String] => List[String]]](pickled)
      val l3 = List("four")
      assert(unpickled.unwrap()(l3) == List("four", "three"))
      assert(unpickled == spore)
    }
  }

}
