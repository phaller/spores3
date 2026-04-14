package spores.jvm

import utest.*
import upickle.default.*

import spores.default.given
import spores.default.*
import spores.conversions.given
import spores.TestUtils.*


object SporeLambdaTestsDefs {
  val lambda = Spore.apply[Int => Boolean]() { x => x > 10 }

  val lambdaWithEnv = Spore.apply[Int => Boolean]() { x => x > 10 }.withEnv(11)

  object NestedLambda:
    val lambda = Spore.apply[Int => Boolean]() { x => x > 10 }

  def methodLambda(): Spore[Int => Boolean] =
    Spore.apply[Int => Boolean]() { x => x > 10 }

  def methodLambdaWithUnnusedArg(x: Int): Spore[Int => Boolean] =
    Spore.apply[Int => Boolean]() { y => y > 10 }

  inline def inlinedMethodLambda(): Spore[Int => Boolean] =
    Spore.apply[Int => Boolean]() { x => x > 10 }

  inline def inlinedMethodLambdaWithArg(x: Int): Spore[Int => Boolean] =
    Spore.apply[Int => Boolean]() { y => y > x }

  class ClassWithLambda() {
    val lambda = Spore.apply[Int => Boolean]() { x => x > 10 }
    def methodLambda() = Spore.apply[Int => Boolean]() { x => x > 10 }
  }

  case class Foo(val y: Int) {
    import Foo.given
    def lambda1 = Spore.apply[Int => Boolean](this) { x => x > this.y }
    def lambda2 = Spore.apply[Int => Boolean](y) { x => x > y }
    def lambda3 = Spore.apply[Int => Boolean](*) { x => x > this.y }
    def lambda4 = Spore.apply[Int => Boolean](*) { x => x > y }
  }
  object Foo {
    given ReadWriter[Foo] = macroRW
    given Spore[ReadWriter[Foo]] = Spore(*)(summon)
  }

  inline def inlineApply0[T]()(inline body: T): Spore[T] = {
    Spore.apply()(body)
  }

  inline def inlineApply1[E1, T](inline captures1: E1)(inline body: T)(using ev1: Spore[ReadWriter[E1]]): Spore[T] = {
    Spore.apply(captures1)(body)
  }

  inline def inlineApply2[E1, E2, T](inline captures1: E1, captures2: E2)(inline body: T)(using ev1: Spore[ReadWriter[E1]], ev2: Spore[ReadWriter[E2]]): Spore[T] = {
    Spore.apply(captures1, captures2)(body)
  }

  inline def inlineApplySeq[T](inline captures: Any*)(inline body: T): Spore[T] = {
    Spore.apply(captures*)(body)
  }

  inline def `inlineApply*`[T](inline `*`: Spore0.CaptureAllMode)(inline body: T): Spore[T] = {
    Spore.apply(*)(body)
  }
}

object SporeLambdaTests extends TestSuite {
  import SporeLambdaTestsDefs.*

  val tests = Tests {
    test("testLambda") {
      val predicate = lambda
      assert(predicate(11))
      assert(!predicate(9))
      assert(predicate.get()(11))
      assert(!predicate.get()(9))
    }
  
    test("testLambdaWithEnv") {
      val predicate9 = Spore.apply() { (x: Int) => x > 10 }.withEnv(9)
      val predicate11 = Spore.apply() { (x: Int) => x > 10 }.withEnv(11)
      assert(!predicate9.get())
      assert(predicate11.get())
    }
  
    test("testLambdaWithCtx") {
      val predicate9 = Spore.apply[Int ?=> Boolean]() { summon[Int] > 10 }.withCtx(9)
      val predicate11 = Spore.apply[Int ?=> Boolean]() { summon[Int] > 10 }.withCtx(11)
      assert(!predicate9.get())
      assert(predicate11.get())
    }
  
    test("testPackBuildHigherOrderLambda") {
      val higherLevelFilter = Spore.apply[Spore[Int => Boolean] => Int => Option[Int]]() { env => x => if env.get().apply(x) then Some(x) else None }
      val filter = higherLevelFilter.withEnv(lambda)
      assert(Some(11) == filter(11))
      assert(None == filter(9))
      assert(Some(11) == filter.get()(11))
      assert(None == filter.get()(9))
    }
  
    test("testPackedLambdaReadWriter") {
      val json = """{"tag":"Body","kind":2,"className":"spores.jvm.SporeLambdaTestsDefs$Lambda$1"}"""
  
      val packed = upickle.default.write(lambda)
      assert(json == packed)
  
      val loaded = upickle.default.read[Spore[Int => Boolean]](json)
      assert(loaded(11))
      assert(!loaded(9))
      assert(loaded.get()(11))
      assert(!loaded.get()(9))
    }
  
    test("testNestedLambdaReadWriter") {
      val json = """{"tag":"Body","kind":2,"className":"spores.jvm.SporeLambdaTestsDefs$NestedLambda$Lambda$3"}"""
  
      val packed = upickle.default.write(NestedLambda.lambda)
      assert(json == packed)
  
      val loaded = upickle.default.read[Spore[Int => Boolean]](json)
      assert(loaded(11))
      assert(!loaded(9))
      assert(loaded.get()(11))
      assert(!loaded.get()(9))
    }
  
    test("testPackedLambdaWithEnvReadWriter") {
      val json9 = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeLambdaTestsDefs$Lambda$1"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":9}}"""
      val json11 = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeLambdaTestsDefs$Lambda$1"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":11}}"""
  
      val packed9 = upickle.default.write(lambda.withEnv(9))
      val packed11 = upickle.default.write(lambda.withEnv(11))
      assert(json9 == packed9)
      assert(json11 == packed11)
  
      val loaded9 = upickle.default.read[Spore[Boolean]](json9).get()
      val loaded11 = upickle.default.read[Spore[Boolean]](json11).get()
      assert(!loaded9)
      assert(loaded11)
    }
  
    test("testLambdaWithEnvConstructorReadWriter") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":2,"className":"spores.jvm.SporeLambdaTestsDefs$Lambda$2"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":11}}"""
  
      val packed = upickle.default.write(lambdaWithEnv)
      assert(json == packed)
  
      val loaded = upickle.default.read[Spore[Boolean]](json).get()
      assert(loaded)
    }
  
    test("testLambdaWithOptionEnvironment") {
      val packed = Spore.apply() { (x: Option[Int]) => x.getOrElse(0) }.withEnv(Some(11))
      val fun = packed.get()
      assert(11 == fun)
    }
  
    test("testLambdaWithListEnvironment") {
      val packed = Spore.apply() { (x: List[Int]) => x.sum }.withEnv(List(1, 2, 3))
      val fun = packed.get()
      assert(6 == fun)
    }
  
    test("testLambdaFromMethodCreator") {
      val packed = methodLambda()
      val fun = packed.get()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromMethodCreatorWithUnnusedArg") {
      val packed = methodLambdaWithUnnusedArg(11)
      val fun = packed.get()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromInlinedMethodCreator") {
      val packed = inlinedMethodLambda()
      val fun = packed.get()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromInlinedMethodCreatorWithArg") {
      val packed = inlinedMethodLambdaWithArg(10)
      val fun = packed.get()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromClassCreator") {
      val packed = ClassWithLambda().lambda
      val fun = packed.get()
      assert(fun(11))
      assert(!fun(9))
    }
  
    test("testLambdaFromClassMethodCreator") {
      val packed = ClassWithLambda().methodLambda()
      val fun = packed.get()
      assert(fun(11))
      assert(!fun(9))
    }

    test("testLambdaCaptureThis") {
      val foo10 = Foo(10)
      assert(foo10.lambda1.get()(11))
      assert(!foo10.lambda1.get()(9))
      assert(foo10.lambda2.get()(11))
      assert(!foo10.lambda2.get()(9))
      assert(foo10.lambda3.get()(11))
      assert(!foo10.lambda3.get()(9))
      assert(foo10.lambda4.get()(11))
      assert(!foo10.lambda4.get()(9))
    }

    test("testNestedExplicitCapture") {
      val c01 = 1;  val c02 = 2;  val c03 = 3;  val c04 = 4;  val c05 = 5
      val c06 = 6;  val c07 = 7;  val c08 = 8;  val c09 = 9;  val c10 = 10
      val c11 = 11; val c12 = 12; val c13 = 13; val c14 = 14; val c15 = 15

      val s = Spore.apply(c01, c02, c03, c04, c05, c06, c07, c08, c09, c10, c11, c12, c13, c14, c15) {
        (a: Int) =>
          val s2 = Spore.apply(c01, c02, c03, c04, c05, c06, c07, c08, c09, c10) {
            (b: Int) =>
              val s3 = Spore.apply(c01, c02, c03, c04, c05, c06) {
                (c: Int) =>
                  val s4 = Spore.apply(c01, c02, c03) { c01 + c02 + c03 }
                  s4.get() + c04 + c05 + c06 + c
              }
              s3.get()(b) + c07 + c08 + c09 + c10
          }
          s2.get()(a) + c11 + c12 + c13 + c14 + c15
      }
      val res = s.get()(100)
      assert(res == 220)
    }

    test("testNestedStarCapture") {
      val c01 = 1;  val c02 = 2;  val c03 = 3;  val c04 = 4;  val c05 = 5
      val c06 = 6;  val c07 = 7;  val c08 = 8;  val c09 = 9;  val c10 = 10
      val c11 = 11; val c12 = 12; val c13 = 13; val c14 = 14; val c15 = 15

      val s = Spore(*) {
        (a: Int) =>
          val s2 = Spore(*) {
            (b: Int) =>
              val s3 = Spore(*) {
                (c: Int) =>
                  val s4 = Spore(*) { c01 + c02 + c03 }
                  s4.get() + c04 + c05 + c06 + c
              }
              s3.get()(b) + c07 + c08 + c09 + c10
          }
          s2.get()(a) + c11 + c12 + c13 + c14 + c15
      }
      val res = s.get()(100)
      assert(res == 220)
    }

    test("testNestedMixedExplicitAndStar") {
      val c01 = 1;  val c02 = 2;  val c03 = 3;  val c04 = 4;  val c05 = 5
      val c06 = 6;  val c07 = 7;  val c08 = 8;  val c09 = 9;  val c10 = 10
      val c11 = 11; val c12 = 12; val c13 = 13; val c14 = 14; val c15 = 15

      val s = Spore.apply(c01, c02, c03, c04, c05, c06, c07, c08, c09, c10, c11, c12, c13, c14, c15) {
        (a: Int) =>
          val s2 = Spore(*) {
            (b: Int) =>
              val s3 = Spore.apply(c01, c02, c03, c04, c05, c06) {
                (c: Int) =>
                  val s4 = Spore(*) { c01 + c02 + c03 }
                  s4.get() + c04 + c05 + c06 + c
              }
              s3.get()(b) + c07 + c08 + c09 + c10
          }
          s2.get()(a) + c11 + c12 + c13 + c14 + c15
      }
      val res = s.get()(100)
      assert(res == 220)
    }

    test("testInlineApply0") {
      val spore = inlineApply0() { (x: Int) => x + 1 }
      val fun = spore.get()
      assert(fun.apply(12) == 13)
      assert(fun.apply(3) == 4)
    }

    test("testInlineApply1") {
      val y = 12
      val spore = inlineApply1(y) { (x: Int) => x > y }
      val fun = spore.get()
      assert(fun.apply(13) == true)
      assert(fun.apply(11) == false)
    }

    test("testInlineApply2") {
      val y = 12
      val z = 13
      val spore = inlineApply2(y, z) { (x: Int) => x + y + z }
      val fun = spore.get()
      assert(fun.apply(11) == 36)
      assert(fun.apply(12) == 37)
    }

    test("testInlineApplySeq") {
      val y = 12
      val z = 13
      val spore = inlineApplySeq(y, z) { (x: Int) => x + y + z }
      val fun = spore.get()
      assert(fun.apply(11) == 36)
      assert(fun.apply(12) == 37)
    }

    test("testInlineApplySeq*") {
      val y = 12
      val z = 13
      val spore = inlineApplySeq(*) { (x: Int) => x + y + z }
      val fun = spore.get()
      assert(fun.apply(11) == 36)
      assert(fun.apply(12) == 37)
    }

    test("testInlineApply*") {
      val y = 12
      val z = 13
      val spore = `inlineApply*`(*) { (x: Int) => x + y + z }
      val fun = spore.get()
      assert(fun.apply(11) == 36)
      assert(fun.apply(12) == 37)
    }

    // Deprecated
    // test("testSporeApplyWithEnvAlias") {
    //   val spore  = Spore.apply[Int, Int => Boolean](12) { env => x => x > env }
    //   val fun = spore.get()
    //   assert(fun(13))
    //   assert(!fun(11))
    // }
  
    // test("testSporeApplyWithEnvAlias2") {
    //   val spore = Spore.apply("Hello") { (env: String) => (x: Int) => x.toString() + env }
    //   val fun = spore.get()
    //   assert("12Hello" == fun(12))
    // }
  }
}
