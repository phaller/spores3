package spores.jvm

import utest._

import spores.default.given
import spores.default.*
import spores.TestUtils.*

object SporeLambdaTestsDefs {
  val lambda = Spore.apply[Int => Boolean] { x => x > 10 }

  val lambdaWithEnv = Spore.applyWithEnv(11) { x => x > 10 }

  object NestedLambda:
    val lambda = Spore.apply[Int => Boolean] { x => x > 10 }

  def methodLambda(): Spore[Int => Boolean] =
    Spore.apply[Int => Boolean] { x => x > 10 }

  def methodLambdaWithUnnusedArg(x: Int): Spore[Int => Boolean] =
    Spore.apply[Int => Boolean] { y => y > 10 }

  inline def inlinedMethodLambda(): Spore[Int => Boolean] =
    Spore.apply[Int => Boolean] { x => x > 10 }

  inline def inlinedMethodLambdaWithArg(x: Int): Spore[Int => Boolean] =
    Spore.apply[Int => Boolean] { y => y > x }

  class ClassWithLambda():
    val lambda = Spore.apply[Int => Boolean] { x => x > 10 }
    def methodLambda() = Spore.apply[Int => Boolean] { x => x > 10 }
}

object SporeLambdaTests extends TestSuite {
  import SporeLambdaTestsDefs.*

  val tests = Tests {
    test("testLambda") {
      val predicate = lambda
      assert(predicate(11))
      assert(!predicate(9))
      assert(predicate.unwrap()(11))
      assert(!predicate.unwrap()(9))
    }
  
    test("testLambdaWithEnv") {
      val predicate9 = Spore.applyWithEnv(9) { x => x > 10 }
      val predicate11 = Spore.applyWithEnv(11) { x => x > 10 }
      assert(!predicate9.unwrap())
      assert(predicate11.unwrap())
    }
  
    test("testLambdaWithCtx") {
      val predicate9 = Spore.applyWithCtx(9) { summon[Int] > 10 }
      val predicate11 = Spore.applyWithCtx(11) { summon[Int] > 10 }
      assert(!predicate9.unwrap())
      assert(predicate11.unwrap())
    }
  
    test("testPackBuildHigherOrderLambda") {
      val higherLevelFilter = Spore.apply[Spore[Int => Boolean] => Int => Option[Int]] { env => x => if env.unwrap().apply(x) then Some(x) else None }
      val filter = higherLevelFilter.withEnv(lambda)
      assert(Some(11) == filter(11))
      assert(None == filter(9))
      assert(Some(11) == filter.unwrap()(11))
      assert(None == filter.unwrap()(9))
    }
  
    test("testPackedLambdaReadWriter") {
      val json = """{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTestsDefs$Lambda$1"}"""
  
      val packed = upickle.default.write(lambda)
      assert(json == packed)
  
      val loaded = upickle.default.read[Spore[Int => Boolean]](json)
      assert(loaded(11))
      assert(!loaded(9))
      assert(loaded.unwrap()(11))
      assert(!loaded.unwrap()(9))
    }
  
    test("testNestedLambdaReadWriter") {
      val json = """{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTestsDefs$NestedLambda$Lambda$3"}"""
  
      val packed = upickle.default.write(NestedLambda.lambda)
      assert(json == packed)
  
      val loaded = upickle.default.read[Spore[Int => Boolean]](json)
      assert(loaded(11))
      assert(!loaded(9))
      assert(loaded.unwrap()(11))
      assert(!loaded.unwrap()(9))
    }
  
    test("testPackedLambdaWithEnvReadWriter") {
      val json9 = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTestsDefs$Lambda$1"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"9","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$IntRW$"}}}"""
      val json11 = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTestsDefs$Lambda$1"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"11","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$IntRW$"}}}"""
  
      val packed9 = upickle.default.write(lambda.withEnv(9))
      val packed11 = upickle.default.write(lambda.withEnv(11))
      assert(json9 == packed9)
      assert(json11 == packed11)
  
      val loaded9 = upickle.default.read[Spore[Boolean]](json9).unwrap()
      val loaded11 = upickle.default.read[Spore[Boolean]](json11).unwrap()
      assert(!loaded9)
      assert(loaded11)
    }
  
    test("testLambdaWithEnvConstructorReadWriter") {
      val json = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTestsDefs$Lambda$2"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"11","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$IntRW$"}}}"""
  
      val packed = upickle.default.write(lambdaWithEnv)
      assert(json == packed)
  
      val loaded = upickle.default.read[Spore[Boolean]](json).unwrap()
      assert(loaded)
    }
  
    test("testLambdaWithOptionEnvironment") {
      val packed = Spore.applyWithEnv(Some(11)) { x => x.getOrElse(0) }
      val fun = packed.unwrap()
      assert(11 == fun)
    }
  
    test("testLambdaWithListEnvironment") {
      val packed = Spore.applyWithEnv(List(1, 2, 3)) { x => x.sum }
      val fun = packed.unwrap()
      assert(6 == fun)
    }
  
    test("testLambdaFromMethodCreator") {
      val packed = methodLambda()
      val fun = packed.unwrap()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromMethodCreatorWithUnnusedArg") {
      val packed = methodLambdaWithUnnusedArg(11)
      val fun = packed.unwrap()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromInlinedMethodCreator") {
      val packed = inlinedMethodLambda()
      val fun = packed.unwrap()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromInlinedMethodCreatorWithArg") {
      val packed = inlinedMethodLambdaWithArg(10)
      val fun = packed.unwrap()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromClassCreator") {
      val packed = ClassWithLambda().lambda
      val fun = packed.unwrap()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromClassMethodCreator") {
      val packed = ClassWithLambda().methodLambda()
      val fun = packed.unwrap()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testSporeApplyWithEnvAlias") {
      val spore  = Spore.apply[Int, Int => Boolean](12) { env => x => x > env }
      val fun = spore.unwrap()
      assert(fun(13))
      assert(!fun(11))
    }
  
    test("testSporeApplyWithEnvAlias2") {
      val spore = Spore.apply("Hello") { (env: String) => (x: Int) => x.toString() + env }
      val fun = spore.unwrap()
      assert("12Hello" == fun(12))
    }
  }
}
