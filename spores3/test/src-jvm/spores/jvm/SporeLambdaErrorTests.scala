package spores.jvm

import utest._

import spores.default.given
import spores.default.*
import spores.TestUtils.*

// // The following code should produce a compile error:
// // Invalid capture of variable `x`. Use the first parameter of a spore's body to refer to the spore's environment.bloop
// // ... but reproducing it with the typeCheckErrorMessages macro is not possible as the object needs to be non-nested top-level.
// object Issue001:
//   def foo(x: Int): Spore[Int => Boolean] = Spore.apply[Int => Boolean] { y => y > x }

object SporeLambdaErrorTests extends TestSuite {

  val tests = Tests {
    test("testInvalidCaptureIdent") {
      assert:
        typeCheckErrorMessages:
          """
          val y = 12
          Spore.apply[Int => Int] { x => x + y }
          """
        .contains:
          """
          Invalid capture of variable `y`. Use the first parameter of a spore's body to refer to the spore's environment.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          Spore.apply[Int => Int] { x => Spore.apply[Int => Int] { y => x + y }.get().apply(x) }
          """
        .contains:
          """
          Invalid capture of variable `x`. Use the first parameter of a spore's body to refer to the spore's environment.
          """.trim()
    }

    test("testInvalidCaptureMethodParameter") {
      assert:
        typeCheckErrorMessages:
          """
          def fun(x: Int): Spore[Int => Boolean] = Spore.apply[Int => Boolean] { y => y > x }
          """
        .contains:
          """
          Invalid capture of variable `x`. Use the first parameter of a spore's body to refer to the spore's environment.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          object ShouldFail:
            def fun(x: Int): Spore[Int => Boolean] = Spore.apply[Int => Boolean] { y => y > x }
          """
        .contains:
          """
          Invalid capture of variable `x`. Use the first parameter of a spore's body to refer to the spore's environment.
          """.trim()
    }

    test("testInvalidCaptureThis") {
      assert:
        typeCheckErrorMessages:
          """
          class TestClass {
            Spore.apply { () => this.toString() }.get()
          }
          (new TestClass())
          """
        .contains:
          """
          Invalid capture of `this` from outer class.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          class Outer:
            val x = 12
            Spore.apply { () => 42 * x }.get()
          (new Outer())
          """
        .contains:
          """
          Invalid capture of `this` from class Outer.
          """.trim()
    }
  }
}
