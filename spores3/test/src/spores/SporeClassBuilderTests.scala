package spores

import utest._

import spores.default.given
import spores.default.*
import spores.conversions.given
import spores.TestUtils.*


object SporeClassBuilderTestsDefs {
  class Thunk[T] extends SporeClassBuilder[T => () => T] {
    override def body = t => () => t
  }

  class Predicate extends SporeClassBuilder[Int => Boolean] {
    override def body = x => x > 10
  }

  class FilterWithTypeParam[T] extends SporeClassBuilder[Spore[T => Boolean] => T => Option[T]] {
    override def body = { env => x => if env.get().apply(x) then Some(x) else None }
  }

  class Flatten[T] extends SporeClassBuilder[List[List[T]] => List[T]] {
    override def body = x => x.flatten
  }

  object NestedBuilder:
    class Predicate extends SporeClassBuilder[Int => Boolean] {
      override def body = x => x > 10
    }
}

object SporeClassBuilderTests extends TestSuite {
  import SporeClassBuilderTestsDefs.*

  val tests = Tests {
    test("testSporeClassBuilderPack") {
      val predicate = new Predicate().build()
      assert(predicate(11))
      assert(!predicate(9))
      assert(predicate.get()(11))
      assert(!predicate.get()(9))
    }

    test("testSporeClassBuilderWithEnv") {
      val thunk = new Thunk[Int].build().withEnv(10)
      assert(10 == thunk())
      assert(10 == thunk.get()())
    }

    test("testSporeClassBuilderWithTypeParam") {
      val flatten = new Flatten[Int].build()
      val nestedList = List(List(1), List(2), List(3))
      assert(nestedList.flatten == flatten(nestedList))
      assert(nestedList.flatten == flatten.get()(nestedList))
    }

    test("testHigherLevelSporeClassBuilder") {
      val filter = new FilterWithTypeParam[Int].build()
      val predicate = new Predicate().build()
      assert(Some(11) == filter(predicate)(11))
      assert(None == filter(predicate)(9))
      assert(Some(11) == filter.get()(predicate)(11))
      assert(None == filter.get()(predicate)(9))
    }

    test("testSporeClassBuilderReadWriter") {
      val json = """{"tag":"Body","kind":1,"className":"spores.SporeClassBuilderTestsDefs$Predicate"}"""

      val packed = upickle.default.write(new Predicate().build())
      assert(json == packed)

      val loaded = upickle.default.read[Spore[Int => Boolean]](json)
      assert(loaded(11))
      assert(!loaded(9))
      assert(loaded.get()(11))
      assert(!loaded.get()(9))
    }

    test("testSporeClassBuilderWithTypeParamReadWriter") {
      val json = """{"tag":"Body","kind":1,"className":"spores.SporeClassBuilderTestsDefs$Flatten"}"""

      val packed = upickle.default.write(new Flatten[Int].build())
      assert(json == packed)

      val loaded = upickle.default.read[Spore[List[List[Int]] => List[Int]]](json)
      val nestedList = List(List(1), List(2), List(3))
      assert(loaded(nestedList) == nestedList.flatten)
      assert(loaded.get()(nestedList) == nestedList.flatten)
    }

    test("testSporeClassBuilderWithEnvReadWriter") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.SporeClassBuilderTestsDefs$FilterWithTypeParam"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$SporeRW$"},"value":{"tag":"Body","kind":1,"className":"spores.SporeClassBuilderTestsDefs$Predicate"}}}"""

      val predicate = new Predicate().build()
      val filter = new FilterWithTypeParam[Int].build().withEnv(predicate)
      val packed = upickle.default.write(filter)
      assert(json == packed)

      val loaded = upickle.default.read[Spore[Int => Option[Int]]](json)
      assert(Some(11) == loaded(11))
      assert(None == loaded(9))
      assert(Some(11) == loaded.get()(11))
      assert(None == loaded.get()(9))
    }
  }
}
