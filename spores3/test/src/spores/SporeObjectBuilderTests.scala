package spores

import utest._

import spores.default.given
import spores.default.*
import spores.conversions.given
import spores.TestUtils.*

object SporeBuilderTestsDefs {
  object Thunk extends SporeBuilder[() => Int] {
    override def body = () => 10
  }

  object Predicate extends SporeBuilder[Int => Boolean] {
    override def body = x => x > 10
  }

  object HigherLevelFilter extends SporeBuilder[Spore[Int => Boolean] => Int => Option[Int]] {
    override def body = { env => x => if env.get().apply(x) then Some(x) else None }
  }

  object PredicateCtx extends SporeBuilder[Int ?=> Boolean] {
    override def body = summon[Int] > 10
  }

  object OptionMapper extends SporeBuilder[Option[Int] => Int] {
    override def body = x => x.getOrElse(0)
  }

  object ListReducer extends SporeBuilder[List[Int] => Int] {
    override def body = x => x.sum
  }

  object NestedBuilder:
    object Predicate extends SporeBuilder[Int => Boolean] {
      override def body = x => x > 10
    }

  object Funct0 extends SporeBuilder[() => Int] {
    override def body = () => 1
  }

  object Funct1 extends SporeBuilder[(Int) => Int] {
    override def body = x1 => x1 + 1
  }

  object Funct2 extends SporeBuilder[(Int, Int) => Int] {
    override def body = (x1, x2) => x1 + x2 + 1
  }

  object Funct3 extends SporeBuilder[(Int, Int, Int) => Int] {
    override def body = (x1, x2, x3) => x1 + x2 + x3 + 1
  }

  object Funct4 extends SporeBuilder[(Int, Int, Int, Int) => Int] {
    override def body = (x1, x2, x3, x4) => x1 + x2 + x3 + x4 + 1
  }

  object Funct5 extends SporeBuilder[(Int, Int, Int, Int, Int) => Int] {
    override def body = (x1, x2, x3, x4, x5) => x1 + x2 + x3 + x4 + x5 + 1
  }

  object Funct6 extends SporeBuilder[(Int, Int, Int, Int, Int, Int) => Int] {
    override def body = (x1, x2, x3, x4, x5, x6) => x1 + x2 + x3 + x4 + x5 + x6 + 1
  }

  object Funct7 extends SporeBuilder[(Int, Int, Int, Int, Int, Int, Int) => Int] {
    override def body = (x1, x2, x3, x4, x5, x6, x7) => x1 + x2 + x3 + x4 + x5 + x6 + x7 + 1
  }
}

object SporeBuilderTests extends TestSuite {
  import SporeBuilderTestsDefs.*

  val tests = Tests {
    test("testSporeBuilderPack") {
      val predicate = Predicate.build()
      assert(predicate(11))
      assert(!predicate(9))
      val unwrapped = predicate.get()
      assert(unwrapped(11))
      assert(!unwrapped(9))
    }

    test("testNestedSporeBuilderPack") {
      val predicate = NestedBuilder.Predicate.build()
      assert(predicate(11))
      assert(!predicate(9))
      val unwrapped = predicate.get()
      assert(unwrapped(11))
      assert(!unwrapped(9))
    }

    test("testSporeBuilderThunk") {
      val thunk = Thunk.build()
      assert(10 == thunk.apply())
      val unwrapped = thunk.get()
      assert(10 == unwrapped.apply())
    }

    test("testWithEnv") {
      val predicate9 = Predicate.build().withEnv(9)
      val predicate11 = Predicate.build().withEnv(11)
      assert(!predicate9.get())
      assert(predicate11.get())
    }

    test("testWithEnv2") {
      val env9 = Spore.value(9)
      val predicate9 = Predicate.build().withEnv2(env9)
      val env11 = Spore.value(11)
      val predicate11 = Predicate.build().withEnv2(env11)
      assert(!predicate9.get())
      assert(predicate11.get())
    }

    test("testWithCtx") {
      val predicate9 = PredicateCtx.build().withCtx(9)
      val packed11 = PredicateCtx.build().withCtx(11)
      assert(!predicate9.get())
      assert(packed11.get())
    }

    test("testWithCtx2") {
      val env9 = Spore.value(9)
      val predicate9 = PredicateCtx.build().withCtx2(env9)
      val env11 = Spore.value(11)
      val packed11 = PredicateCtx.build().withCtx2(env11)
      assert(!predicate9.get())
      assert(packed11.get())
    }

    test("testPackBuildHigherOrderSporeBuilder") {
      val predicate = Predicate.build()
      val filter = HigherLevelFilter.build().withEnv(predicate)
      assert(Some(11) == filter(11))
      assert(None == filter(9))
      val unwrapped = filter.get()
      assert(Some(11) == unwrapped(11))
      assert(None == unwrapped(9))
    }

    test("testSporeReadWriter") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeBuilderTestsDefs$Predicate$"}"""

      val packed = upickle.default.write(Predicate.build())
      assert(packed == json)

      val loaded = upickle.default.read[Spore[Int => Boolean]](json)
      assert(loaded(11))
      assert(!loaded(9))
      assert(loaded.get()(11))
      assert(!loaded.get()(9))
    }

    test("testNestedSporeReadWriter") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeBuilderTestsDefs$NestedBuilder$Predicate$"}"""

      val packed = upickle.default.write(NestedBuilder.Predicate.build())
      assert(packed == json)

      val loaded = upickle.default.read[Spore[Int => Boolean]](json)
      assert(loaded(11))
      assert(!loaded(9))
      assert(loaded.get()(11))
      assert(!loaded.get()(9))
    }

    test("testSporeReadWriterWithEnv") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeBuilderTestsDefs$HigherLevelFilter$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$SporeRW$"},"value":{"tag":"Body","kind":0,"className":"spores.SporeBuilderTestsDefs$Predicate$"}}}"""

      val predicate = Predicate.build()
      val filter = HigherLevelFilter.build().withEnv(predicate)
      val packed = upickle.default.write(filter)
      assert(packed == json)

      val loaded = upickle.default.read[Spore[Int => Option[Int]]](json)
      assert(Some(11) == loaded(11))
      assert(None == loaded(9))
      assert(Some(11) == loaded.get()(11))
      assert(None == loaded.get()(9))
    }

    test("testOptionEnvironment") {
      val packed = OptionMapper.build().withEnv(Some(11))
      val fun = packed.get()
      assert(11 == fun)

      val packed2 = OptionMapper.build().withEnv(Some(11))
      val fun2 = packed2.get()
      assert(11 == fun2)
    }

    test("testListEnvironment") {
      val packed = ListReducer.build().withEnv(List(1, 2, 3))
      val fun = packed.get()
      assert(6 == fun)

      val packed2 = ListReducer.build().withEnv(List(1, 2, 3))
      val fun2 = packed2.get()
      assert(6 == fun2)
    }

    test("testSporeApplyMethods") {
      val funct0 = Funct0.build()
      val funct1 = Funct1.build()
      val funct2 = Funct2.build()
      val funct3 = Funct3.build()
      val funct4 = Funct4.build()
      val funct5 = Funct5.build()
      val funct6 = Funct6.build()
      val funct7 = Funct7.build()

      assert(1 == funct0.apply())
      assert(2 == funct1.apply(1))
      assert(4 == funct2.apply(1, 2))
      assert(7 == funct3.apply(1, 2, 3))
      assert(11 == funct4.apply(1, 2, 3, 4))
      assert(16 == funct5.apply(1, 2, 3, 4, 5))
      assert(22 == funct6.apply(1, 2, 3, 4, 5, 6))
      assert(29 == funct7.apply(1, 2, 3, 4, 5, 6, 7))
    }
  }
}
