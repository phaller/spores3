package spores.jvm

import utest._
import upickle.default.*

import spores.default.*
import spores.default.given


object SporeSerializationJvmTestsDefs {
  val lambda = Spore.apply[Int => String] { x => x.toString }
  val lambdaWithEnv = Spore.applyWithEnv(12) { (env: Int) => (x: Int) => (env + x).toString }
  val lambdaWithCtx = Spore.applyWithCtx(12) { summon[Int].toString }
  val curriedLambda = Spore.apply[Int => Int => String] { x => y => (x + y).toString() }
  val higherOrderLambda = Spore.apply[Spore[Int => Boolean] => Int => Option[Int]] { env => x =>
    if env.get().apply(x) then Some(x) else None
  }
  val intPredicate = Spore.apply[Int => Boolean] { x => x > 10 }
  val lambdaReturningSpore = Spore.apply[Int => Spore[String]] { x => Spore.value(x.toString()) }
}

object SporeSerializationJvmTestsDeadCode {
  val lambda = Spore.apply[Int => String] { x => x.toString }
  val lambdaWithEnv = Spore.applyWithEnv(12) { (env: Int) => (x: Int) => (env + x).toString }
  val lambdaWithCtx = Spore.applyWithCtx(12) { summon[Int].toString }
  val curriedLambda = Spore.apply[Int => Int => String] { x => y => (x + y).toString() }
  val higherOrderLambda = Spore.apply[Spore[Int => Boolean] => Int => Option[Int]] { env => x =>
    if env.get().apply(x) then Some(x) else None
  }
  val intPredicate = Spore.apply[Int => Boolean] { x => x > 10 }
  val lambdaReturningSpore = Spore.apply[Int => Spore[String]] { x => Spore.value(x.toString()) }
}

object SporeSerializationJvmTests extends TestSuite {
  import SporeSerializationJvmTestsDefs.*

  val tests = Tests {

    test("testSerializeSporeApply") {
      val expected = """{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDefs$Lambda$1"}"""
      val json = write(lambda)
      assert(expected == json)
      val spore2 = read[Spore[Int => String]](json)
      assert("12" == spore2.get()(12))
    }

    test("testSerializeSporeApplyWithEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDefs$Lambda$2"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val json = write(lambdaWithEnv)
      assert(expected == json)
      val spore2 = read[Spore[Int => String]](json)
      assert("20" == spore2.get()(8))
    }

    test("testSerializeSporeApplyWithCtx") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDefs$Lambda$3"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val json = write(lambdaWithCtx)
      assert(expected == json)
      val spore2 = read[Spore[String]](json)
      assert("12" == spore2.get())
    }

    test("testSerializeCurriedLambda") {
      val expected = """{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDefs$Lambda$4"}"""
      val json = write(curriedLambda)
      assert(expected == json)
      val spore2 = read[Spore[Int => Int => String]](json)
      assert("7" == spore2.get()(3)(4))
    }

    test("testSerializeCurriedLambdaWithEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDefs$Lambda$4"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":3}}"""
      val spore = curriedLambda.withEnv(3)
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => String]](json)
      assert("7" == spore2.get()(4))
    }

    test("testSerializeHigherOrderWithSporeEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDefs$Lambda$5"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$SporeRW$"},"value":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDefs$Lambda$6"}}}"""
      val spore = higherOrderLambda.withEnv(intPredicate)
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => Option[Int]]](json)
      assert(Some(11) == spore2.get()(11))
      assert(None == spore2.get()(9))
    }

    test("testSerializeLambdaReturningSpore") {
      val expected = """{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDefs$Lambda$7"}"""
      val json = write(lambdaReturningSpore)
      assert(expected == json)
      val spore2 = read[Spore[Int => Spore[String]]](json)
      assert("12" == spore2.get()(12).get())
    }

    test("testSerializeDeadCodeLambda") {
      val json = """{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDeadCode$Lambda$8"}"""
      val spore2 = read[Spore[Int => String]](json)
      assert("12" == spore2.get()(12))
    }

    test("testSerializeDeadCodeLambdaWithEnv") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDeadCode$Lambda$9"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val spore2 = read[Spore[Int => String]](json)
      assert("20" == spore2.get()(8))
    }

    test("testSerializeDeadCodeLambdaWithCtx") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDeadCode$Lambda$10"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val spore2 = read[Spore[String]](json)
      assert("12" == spore2.get())
    }

    test("testSerializeDeadCodeCurriedLambda") {
      val json = """{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDeadCode$Lambda$11"}"""
      val spore2 = read[Spore[Int => Int => String]](json)
      assert("7" == spore2.get()(3)(4))
    }

    test("testSerializeDeadCodeCurriedLambdaWithEnv") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDeadCode$Lambda$11"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":3}}"""
      val spore2 = read[Spore[Int => String]](json)
      assert("7" == spore2.get()(4))
    }

    test("testSerializeDeadCodeHigherOrderWithSporeEnv") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDeadCode$Lambda$12"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$SporeRW$"},"value":{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDeadCode$Lambda$13"}}}"""
      val spore2 = read[Spore[Int => Option[Int]]](json)
      assert(Some(11) == spore2.get()(11))
      assert(None == spore2.get()(9))
    }

    test("testSerializeDeadCodeLambdaReturningSpore") {
      val json = """{"tag":"Body","kind":2,"className":"spores.jvm.SporeSerializationJvmTestsDeadCode$Lambda$14"}"""
      val spore2 = read[Spore[Int => Spore[String]]](json)
      assert("12" == spore2.get()(12).get())
    }
  }
}
