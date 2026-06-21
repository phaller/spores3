package spores.jvm

import utest._

import spores.default.given
import spores.default.*
import spores.TestUtils.*

// // The following code should produce a compile error:
// // Invalid capture of variable `x`. Add it to the capture list or use `*` to capture all by default.bloop
// // ... but reproducing it with the typeCheckErrorMessages macro is not possible as the object needs to be non-nested top-level.
// object Issue001:
//   def foo(x: Int): Spore[Int => Boolean] = Spore.apply[Int => Boolean]() { y => y > x }

object SporeLambdaErrorTestsDefs {
  val foo = 12
}

object SporeLambdaErrorTests extends TestSuite {
  import SporeLambdaErrorTestsDefs.*

  val tests = Tests {
    test("testInvalidCaptureIdent") {
      assert:
        typeCheckErrorMessages:
          """
          val y = 12
          Spore.apply[Int => Int]() { x => x + y }
          """
        .contains:
          """
          Invalid capture of variable `y`. Add it to the capture list or use `*` to capture all by default.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          Spore.apply[Int => Int]() { x => Spore.apply[Int => Int]() { y => x + y }.get().apply(x) }
          """
        .contains:
          """
          Invalid capture of variable `x`. Add it to the capture list or use `*` to capture all by default.
          """.trim()
    }

    test("testInvalidCaptureMethodParameter") {
      assert:
        typeCheckErrorMessages:
          """
          def fun(x: Int): Spore[Int => Boolean] = Spore.apply[Int => Boolean]() { y => y > x }
          """
        .contains:
          """
          Invalid capture of variable `x`. Add it to the capture list or use `*` to capture all by default.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          object ShouldFail:
            def fun(x: Int): Spore[Int => Boolean] = Spore.apply[Int => Boolean]() { y => y > x }
          """
        .contains:
          """
          Invalid capture of variable `x`. Add it to the capture list or use `*` to capture all by default.
          """.trim()
    }

    test("testInvalidCaptureThis") {
      assert:
        typeCheckErrorMessages:
          """
          class TestClass {
            Spore.apply() { () => this.toString() }.get()
          }
          (new TestClass())
          """
        .contains:
          """
          Invalid capture of `this` from outer class. Add it to the capture list or use `*` to capture all by default.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          class Outer:
            val x = 12
            Spore.apply() { () => 42 * x }.get()
          (new Outer())
          """
        .contains:
          """
          Invalid capture of `this` from class Outer. Add it to the capture list or use `*` to capture all by default.
          """.trim()
    }

    test("testInvalidCaptureOfTopLevelMember") {
      assert:
        typeCheckErrorMessages:
          """
          Spore.apply(foo) { (x: Int) => x + foo + 1 }.get()
          """
        .contains:
          """
          `foo` is not captured by the spore body. Remove it from the capture list. It is a top-level variable or not used in the body.
          """.trim()
    }

    test("testInvalidCaptureOfUnusedMember") {
      assert:
        typeCheckErrorMessages:
          """
          val y = 12
          Spore.apply(y) { (x: Int) => x + 1 }.get()
          """
        .contains:
          """
          `y` is not captured by the spore body. Remove it from the capture list. It is a top-level variable or not used in the body.
          """.trim()
    }

    test("testInvalidCaptureList") {
      assert:
        typeCheckErrorMessages:
          """
          val y = 12
          val z = 13
          Spore.apply(*, y, z) { (x: Int) => x + 1 }.get()
          """
        .contains:
          """
          Invalid capture list.
          """.trim()
    }

    test("testInvalidCaptureIdentInlineApply0") {
      assert:
        typeCheckErrorMessages:
          """
          val y = 12
          SporeLambdaTestsDefs.inlineApply0() { (x: Int) => x + y }
          """
        .contains:
          """
          Invalid capture of variable `y`. Add it to the capture list or use `*` to capture all by default.
          """.trim()
    }

    test("testInvalidCaptureIdentInlineApplySeq") {
      assert:
        typeCheckErrorMessages:
          """
          val y = 12
          SporeLambdaTestsDefs.inlineApplySeq() { (x: Int) => x + y }
          """
        .contains:
          """
          Invalid capture of variable `y`. Add it to the capture list or use `*` to capture all by default.
          """.trim()
    }

    test("testInvalidCaptureListInlineApplySeq") {
      assert:
        typeCheckErrorMessages:
          """
          val y = 12
          val z = 13
          SporeLambdaTestsDefs.inlineApplySeq(y, *, z) { (x: Int) => x + 1 }.get()
          """
        .contains:
          """
          Invalid capture list.
          """.trim()
    }

    test("testInvalidCaptureOfUnusedMemberInlineApply1") {
      assert:
        typeCheckErrorMessages:
          """
          val y = 12
          SporeLambdaTestsDefs.inlineApply1(y) { (x: Int) => x + 1 }
          """
        .contains:
          """
          `y` is not captured by the spore body. Remove it from the capture list. It is a top-level variable or not used in the body.
          """.trim()
    }
  }
}
