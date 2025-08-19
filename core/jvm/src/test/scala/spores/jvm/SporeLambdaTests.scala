package spores.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import spores.default.given
import spores.default.*
import spores.TestUtils.*

object SporeLambdaTests:
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

@RunWith(classOf[JUnit4])
class SporeLambdaTests:
  import SporeLambdaTests.*

  @Test
  def testLambda(): Unit =
    val predicate = lambda
    assertTrue(predicate(11))
    assertFalse(predicate(9))
    assertTrue(predicate.unwrap()(11))
    assertFalse(predicate.unwrap()(9))

  @Test
  def testLambdaWithEnv(): Unit =
    val predicate9 = Spore.applyWithEnv(9) { x => x > 10 }
    val predicate11 = Spore.applyWithEnv(11) { x => x > 10 }
    assertFalse(predicate9.unwrap())
    assertTrue(predicate11.unwrap())

  @Test
  def testLambdaWithCtx(): Unit =
    val predicate9 = Spore.applyWithCtx(9) { summon[Int] > 10 }
    val predicate11 = Spore.applyWithCtx(11) { summon[Int] > 10 }
    assertFalse(predicate9.unwrap())
    assertTrue(predicate11.unwrap())

  @Test
  def testPackBuildHigherOrderLambda(): Unit =
    val higherLevelFilter = Spore.apply[Spore[Int => Boolean] => Int => Option[Int]] { env => x => if env.unwrap().apply(x) then Some(x) else None }
    val filter = higherLevelFilter.withEnv(lambda)
    assertEquals(Some(11), filter(11))
    assertEquals(None, filter(9))
    assertEquals(Some(11), filter.unwrap()(11))
    assertEquals(None, filter.unwrap()(9))

  @Test
  def testPackedLambdaReadWriter(): Unit =
    val json = """{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTests$Lambda$12"}"""

    val packed = upickle.default.write(lambda)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spore[Int => Boolean]](json)
    assertTrue(loaded(11))
    assertFalse(loaded(9))
    assertTrue(loaded.unwrap()(11))
    assertFalse(loaded.unwrap()(9))

  @Test
  def testNestedLambdaReadWriter(): Unit =
    val json = """{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTests$NestedLambda$Lambda$14"}"""

    val packed = upickle.default.write(NestedLambda.lambda)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spore[Int => Boolean]](json)
    assertTrue(loaded(11))
    assertFalse(loaded(9))
    assertTrue(loaded.unwrap()(11))
    assertFalse(loaded.unwrap()(9))

  @Test
  def testPackedLambdaWithEnvReadWriter(): Unit =
    val json9 = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTests$Lambda$12"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"9","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$IntRW$"}}}"""
    val json11 = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTests$Lambda$12"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"11","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$IntRW$"}}}"""

    val packed9 = upickle.default.write(lambda.withEnv(9))
    val packed11 = upickle.default.write(lambda.withEnv(11))
    assertEquals(json9, packed9)
    assertEquals(json11, packed11)

    val loaded9 = upickle.default.read[Spore[Boolean]](json9).unwrap()
    val loaded11 = upickle.default.read[Spore[Boolean]](json11).unwrap()
    assertFalse(loaded9)
    assertTrue(loaded11)

  @Test
  def testLambdaWithEnvConstructorReadWriter(): Unit =
    val json = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.SporeLambdaTests$Lambda$13"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"11","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$IntRW$"}}}"""

    val packed = upickle.default.write(lambdaWithEnv)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spore[Boolean]](json).unwrap()
    assertTrue(loaded)

  @Test
  def testLambdaWithOptionEnvironment(): Unit =
    val packed = Spore.applyWithEnv(Some(11)) { x => x.getOrElse(0) }
    val fun = packed.unwrap()
    assertEquals(11, fun)

  @Test
  def testLambdaWithListEnvironment(): Unit =
    val packed = Spore.applyWithEnv(List(1, 2, 3)) { x => x.sum }
    val fun = packed.unwrap()
    assertEquals(6, fun)

  @Test
  def testLambdaFromMethodCreator(): Unit =
    val packed = methodLambda()
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromMethodCreatorWithUnnusedArg(): Unit =
    val packed = methodLambdaWithUnnusedArg(11)
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromInlinedMethodCreator(): Unit =
    val packed = inlinedMethodLambda()
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromInlinedMethodCreatorWithArg(): Unit =
    val packed = inlinedMethodLambdaWithArg(10)
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromClassCreator(): Unit =
    val packed = ClassWithLambda().lambda
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testLambdaFromClassMethodCreator(): Unit =
    val packed = ClassWithLambda().methodLambda()
    val fun = packed.unwrap()
    assertTrue(fun(11))
    assertFalse(fun(9))

  @Test
  def testSporeApplyWithEnvAlias(): Unit =
    val spore  = Spore.apply[Int, Int => Boolean](12) { env => x => x > env }
    val fun = spore.unwrap()
    assertTrue(fun(13))
    assertFalse(fun(11))
    
  @Test
  def testSporeApplyWithEnvAlias2(): Unit =
    val spore = Spore.apply("Hello") { (env: String) => (x: Int) => x.toString() + env }
    val fun = spore.unwrap()
    assertEquals("12Hello", fun(12))
