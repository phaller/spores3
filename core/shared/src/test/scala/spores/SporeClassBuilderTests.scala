package spores

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import spores.default.given
import spores.default.*
import spores.TestUtils.*

object SporeClassBuilderTests:
  class Thunk[T] extends SporeClassBuilder[T => () => T](t => () => t)

  class Predicate extends SporeClassBuilder[Int => Boolean](x => x > 10)

  class FilterWithTypeParam[T] extends SporeClassBuilder[Spore[T => Boolean] => T => Option[T]]({ env => x => if env.unwrap().apply(x) then Some(x) else None })

  class Flatten[T] extends SporeClassBuilder[List[List[T]] => List[T]](x => x.flatten)

  object NestedBuilder:
    class Predicate extends SporeClassBuilder[Int => Boolean](x => x > 10)

@RunWith(classOf[JUnit4])
class SporeClassBuilderTests:
  import SporeClassBuilderTests.*

  @Test
  def testSporeClassBuilderPack(): Unit =
    val predicate = new Predicate().build()
    assertTrue(predicate(11))
    assertFalse(predicate(9))
    assertTrue(predicate.unwrap()(11))
    assertFalse(predicate.unwrap()(9))

  @Test
  def testSporeClassBuilderWithEnv(): Unit =
    val thunk = new Thunk[Int].build().withEnv(10)
    assertEquals(10, thunk())
    assertEquals(10, thunk.unwrap()())

  @Test
  def testSporeClassBuilderWithTypeParam(): Unit =
    val flatten = new Flatten[Int].build()
    val nestedList = List(List(1), List(2), List(3))
    assertEquals(nestedList.flatten, flatten(nestedList))
    assertEquals(nestedList.flatten, flatten.unwrap()(nestedList))

  @Test
  def testHigherLevelSporeClassBuilder(): Unit =
    val filter = new FilterWithTypeParam[Int].build()
    val predicate = new Predicate().build()
    assertEquals(Some(11), filter(predicate)(11))
    assertEquals(None, filter(predicate)(9))
    assertEquals(Some(11), filter.unwrap()(predicate)(11))
    assertEquals(None, filter.unwrap()(predicate)(9))

  @Test
  def testSporeClassBuilderReadWriter(): Unit =
    val json = """{"$type":"spores.Packed.PackedClass","className":"spores.SporeClassBuilderTests$Predicate"}"""

    val packed = upickle.default.write(new Predicate().build())
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spore[Int => Boolean]](json)
    assertTrue(loaded(11))
    assertFalse(loaded(9))
    assertTrue(loaded.unwrap()(11))
    assertFalse(loaded.unwrap()(9))

  @Test
  def testSporeClassBuilderWithTypeParamReadWriter(): Unit =
    val json = """{"$type":"spores.Packed.PackedClass","className":"spores.SporeClassBuilderTests$Flatten"}"""

    val packed = upickle.default.write(new Flatten[Int].build())
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spore[List[List[Int]] => List[Int]]](json)
    val nestedList = List(List(1), List(2), List(3))
    assertEquals(nestedList.flatten, loaded(nestedList))
    assertEquals(nestedList.flatten, loaded.unwrap()(nestedList))

  @Test
  def testSporeClassBuilderWithEnvReadWriter(): Unit =
    val json = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedClass","className":"spores.SporeClassBuilderTests$FilterWithTypeParam"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"{\"$type\":\"spores.Packed.PackedClass\",\"className\":\"spores.SporeClassBuilderTests$Predicate\"}","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$SporeRW$"}}}"""

    val predicate = new Predicate().build()
    val filter = new FilterWithTypeParam[Int].build().withEnv(predicate)
    val packed = upickle.default.write(filter)
    assertEquals(json, packed)

    val loaded = upickle.default.read[Spore[Int => Option[Int]]](json)
    assertEquals(Some(11), loaded(11))
    assertEquals(None, loaded(9))
    assertEquals(Some(11), loaded.unwrap()(11))
    assertEquals(None, loaded.unwrap()(9))
