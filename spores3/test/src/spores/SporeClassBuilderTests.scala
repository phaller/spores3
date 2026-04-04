package spores

import utest._

import spores.default.given
import spores.default.*
import spores.TestUtils.*

object SporeClassBuilderTestsDefs {
  class Thunk[T] extends SporeClassBuilder[T => () => T](t => () => t)

  class Predicate extends SporeClassBuilder[Int => Boolean](x => x > 10)

  class FilterWithTypeParam[T] extends SporeClassBuilder[Spore[T => Boolean] => T => Option[T]]({ env => x => if env.unwrap().apply(x) then Some(x) else None })

  class Flatten[T] extends SporeClassBuilder[List[List[T]] => List[T]](x => x.flatten)

  object NestedBuilder:
    class Predicate extends SporeClassBuilder[Int => Boolean](x => x > 10)
}

object SporeClassBuilderTests extends TestSuite {
  import SporeClassBuilderTestsDefs.*

  val tests = Tests {
    test("testSporeClassBuilderPack") {
      val predicate = new Predicate().build()
      assert(predicate(11))
      assert(!predicate(9))
      assert(predicate.unwrap()(11))
      assert(!predicate.unwrap()(9))
    }

    test("testSporeClassBuilderWithEnv") {
      val thunk = new Thunk[Int].build().withEnv(10)
      assert(10 == thunk())
      assert(10 == thunk.unwrap()())
    }

    test("testSporeClassBuilderWithTypeParam") {
      val flatten = new Flatten[Int].build()
      val nestedList = List(List(1), List(2), List(3))
      assert(nestedList.flatten == flatten(nestedList))
      assert(nestedList.flatten == flatten.unwrap()(nestedList))
    }

    test("testHigherLevelSporeClassBuilder") {
      val filter = new FilterWithTypeParam[Int].build()
      val predicate = new Predicate().build()
      assert(Some(11) == filter(predicate)(11))
      assert(None == filter(predicate)(9))
      assert(Some(11) == filter.unwrap()(predicate)(11))
      assert(None == filter.unwrap()(predicate)(9))
    }

    test("testSporeClassBuilderReadWriter") {
      val json = """{"$type":"spores.Packed.PackedClass","className":"spores.SporeClassBuilderTestsDefs$Predicate"}"""

      val packed = upickle.default.write(new Predicate().build())
      assert(json == packed)

      val loaded = upickle.default.read[Spore[Int => Boolean]](json)
      assert(loaded(11))
      assert(!loaded(9))
      assert(loaded.unwrap()(11))
      assert(!loaded.unwrap()(9))
    }

    test("testSporeClassBuilderWithTypeParamReadWriter") {
      val json = """{"$type":"spores.Packed.PackedClass","className":"spores.SporeClassBuilderTestsDefs$Flatten"}"""

      val packed = upickle.default.write(new Flatten[Int].build())
      assert(json == packed)

      val loaded = upickle.default.read[Spore[List[List[Int]] => List[Int]]](json)
      val nestedList = List(List(1), List(2), List(3))
      assert(loaded(nestedList) == nestedList.flatten)
      assert(loaded.unwrap()(nestedList) == nestedList.flatten)
    }

    test("testSporeClassBuilderWithEnvReadWriter") {
      val json = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedClass","className":"spores.SporeClassBuilderTestsDefs$FilterWithTypeParam"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"{\"$type\":\"spores.Packed.PackedClass\",\"className\":\"spores.SporeClassBuilderTestsDefs$Predicate\"}","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$SporeRW$"}}}"""

      val predicate = new Predicate().build()
      val filter = new FilterWithTypeParam[Int].build().withEnv(predicate)
      val packed = upickle.default.write(filter)
      assert(json == packed)

      val loaded = upickle.default.read[Spore[Int => Option[Int]]](json)
      assert(Some(11) == loaded(11))
      assert(None == loaded(9))
      assert(Some(11) == loaded.unwrap()(11))
      assert(None == loaded.unwrap()(9))
    }
  }
}
