package spores

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import spores.default.given
import spores.default.*
import spores.TestUtils.*

object SporeBuilderTests:
  object Thunk extends SporeBuilder[() => Int](() => 10)

  object Predicate extends SporeBuilder[Int => Boolean](x => x > 10)

  object HigherLevelFilter extends SporeBuilder[Spore[Int => Boolean] => Int => Option[Int]]({ env => x => if env.unwrap().apply(x) then Some(x) else None })

  object PredicateCtx extends SporeBuilder[Int ?=> Boolean](summon[Int] > 10)

  object OptionMapper extends SporeBuilder[Option[Int] => Int](x => x.getOrElse(0))

  object ListReducer extends SporeBuilder[List[Int] => Int](x => x.sum)

  object NestedBuilder:
    object Predicate extends SporeBuilder[Int => Boolean](x => x > 10)

  object Funct0 extends SporeBuilder[() => Int](() => 1)

  object Funct1 extends SporeBuilder[(Int) => Int](x1 => x1 + 1)

  object Funct2 extends SporeBuilder[(Int, Int) => Int]((x1, x2) => x1 + x2 + 1)

  object Funct3 extends SporeBuilder[(Int, Int, Int) => Int]((x1, x2, x3) => x1 + x2 + x3 + 1)

  object Funct4 extends SporeBuilder[(Int, Int, Int, Int) => Int]((x1, x2, x3, x4) => x1 + x2 + x3 + x4 + 1)

  object Funct5 extends SporeBuilder[(Int, Int, Int, Int, Int) => Int]((x1, x2, x3, x4, x5) => x1 + x2 + x3 + x4 + x5 + 1)

  object Funct6 extends SporeBuilder[(Int, Int, Int, Int, Int, Int) => Int]((x1, x2, x3, x4, x5, x6) => x1 + x2 + x3 + x4 + x5 + x6 + 1)

  object Funct7 extends SporeBuilder[(Int, Int, Int, Int, Int, Int, Int) => Int]((x1, x2, x3, x4, x5, x6, x7) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + 1)

@RunWith(classOf[JUnit4])
class SporeBuilderTests:
  import SporeBuilderTests.*

  @Test
  def testSporeBuilderPack(): Unit =
    val predicate = Predicate.build()
    assertTrue(predicate(11))
    assertFalse(predicate(9))
    val unwrapped = predicate.unwrap()
    assertTrue(unwrapped(11))
    assertFalse(unwrapped(9))

  @Test
  def testNestedSporeBuilderPack(): Unit =
    val predicate = NestedBuilder.Predicate.build()
    assertTrue(predicate(11))
    assertFalse(predicate(9))
    val unwrapped = predicate.unwrap()
    assertTrue(unwrapped(11))
    assertFalse(unwrapped(9))

  @Test
  def testSporeBuilderThunk(): Unit =
    val thunk = Thunk.build()
    assertEquals(10, thunk.apply())
    val unwrapped = thunk.unwrap()
    assertEquals(10, unwrapped.apply())

  @Test
  def testWithEnv(): Unit =
    val predicate9 = Predicate.build().withEnv(9)
    val predicate11 = Predicate.build().withEnv(11)
    assertFalse(predicate9.unwrap())
    assertTrue(predicate11.unwrap())

  @Test
  def testWithEnv2(): Unit =
    val env9 = Env.apply(9)
    val predicate9 = Predicate.build().withEnv2(env9)
    val env11 = Env.apply(11)
    val predicate11 = Predicate.build().withEnv2(env11)
    assertFalse(predicate9.unwrap())
    assertTrue(predicate11.unwrap())

  @Test
  def testWithCtx(): Unit =
    val predicate9 = PredicateCtx.build().withCtx(9)
    val packed11 = PredicateCtx.build().withCtx(11)
    assertFalse(predicate9.unwrap())
    assertTrue(packed11.unwrap())

  @Test
  def testWithCtx2(): Unit =
    val env9 = Env.apply(9)
    val predicate9 = PredicateCtx.build().withCtx2(env9)
    val env11 = Env.apply(11)
    val packed11 = PredicateCtx.build().withCtx2(env11)
    assertFalse(predicate9.unwrap())
    assertTrue(packed11.unwrap())

  @Test
  def testPackBuildHigherOrderSporeBuilder(): Unit =
    val predicate = Predicate.build()
    val filter = HigherLevelFilter.build().withEnv(predicate)
    assertEquals(Some(11), filter(11))
    assertEquals(None, filter(9))
    val unwrapped = filter.unwrap()
    assertEquals(Some(11), unwrapped(11))
    assertEquals(None, unwrapped(9))

  @Test
  def testSporeReadWriter(): Unit =
    val json = """{"$type":"spores.Packed.PackedObject","funName":"spores.SporeBuilderTests$Predicate$"}"""

    val packed = upickle.default.write(Predicate.build())
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spore[Int => Boolean]](json)
    assertTrue(loaded(11))
    assertFalse(loaded(9))
    assertTrue(loaded.unwrap()(11))
    assertFalse(loaded.unwrap()(9))

  @Test
  def testNestedSporeReadWriter(): Unit =
    val json = """{"$type":"spores.Packed.PackedObject","funName":"spores.SporeBuilderTests$NestedBuilder$Predicate$"}"""

    val packed = upickle.default.write(NestedBuilder.Predicate.build())
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spore[Int => Boolean]](json)
    assertTrue(loaded(11))
    assertFalse(loaded(9))
    assertTrue(loaded.unwrap()(11))
    assertFalse(loaded.unwrap()(9))

  @Test
  def testSporeReadWriterWithEnv(): Unit =
    val json = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedObject","funName":"spores.SporeBuilderTests$HigherLevelFilter$"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"{\"$type\":\"spores.Packed.PackedObject\",\"funName\":\"spores.SporeBuilderTests$Predicate$\"}","rw":{"$type":"spores.Packed.PackedObject","funName":"spores.ReadWriters$SporeRW$"}}}"""

    val predicate = Predicate.build()
    val filter = HigherLevelFilter.build().withEnv(predicate)
    val packed = upickle.default.write(filter)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spore[Int => Option[Int]]](json)
    assertEquals(Some(11), loaded(11))
    assertEquals(None, loaded(9))
    assertEquals(Some(11), loaded.unwrap()(11))
    assertEquals(None, loaded.unwrap()(9))

  @Test
  def testOptionEnvironment(): Unit =
    val packed = OptionMapper.build().withEnv(Some(11))
    val fun = packed.unwrap()
    assertEquals(11, fun)

    val packed2 = OptionMapper.build().withEnv(Some(11))
    val fun2 = packed2.unwrap()
    assertEquals(11, fun2)

  @Test
  def testListEnvironment(): Unit =
    val packed = ListReducer.build().withEnv(List(1, 2, 3))
    val fun = packed.unwrap()
    assertEquals(6, fun)

    val packed2 = ListReducer.build().withEnv(List(1, 2, 3))
    val fun2 = packed2.unwrap()
    assertEquals(6, fun2)

  @Test
  def testSporeApplyMethods(): Unit =
    val funct0 = Funct0.build()
    val funct1 = Funct1.build()
    val funct2 = Funct2.build()
    val funct3 = Funct3.build()
    val funct4 = Funct4.build()
    val funct5 = Funct5.build()
    val funct6 = Funct6.build()
    val funct7 = Funct7.build()

    assertEquals(1, funct0.apply())
    assertEquals(2, funct1.apply(1))
    assertEquals(4, funct2.apply(1, 2))
    assertEquals(7, funct3.apply(1, 2, 3))
    assertEquals(11, funct4.apply(1, 2, 3, 4))
    assertEquals(16, funct5.apply(1, 2, 3, 4, 5))
    assertEquals(22, funct6.apply(1, 2, 3, 4, 5, 6))
    assertEquals(29, funct7.apply(1, 2, 3, 4, 5, 6, 7))
