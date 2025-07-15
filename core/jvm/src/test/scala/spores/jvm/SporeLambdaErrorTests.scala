package spores.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import spores.given
import spores.*
import spores.TestUtils.*

// // The following code should produce a compile error:
// // Invalid capture of variable `x`. Use the first parameter of a spore's body to refer to the spore's environment.bloop
// // ... but reproducing it with the typeCheckErrorMessages macro is not possible as the object needs to be non-nested top-level.
// object Issue001:
//   def foo(x: Int): Spore[Int => Boolean] = Spore.apply[Int => Boolean] { y => y > x }

@RunWith(classOf[JUnit4])
class SporeLambdaErrorTests:

  @Test
  def testInvalidCaptureIdent(): Unit =
    assertTrue:
      typeCheckErrorMessages:
        """
        val y = 12
        Spore.apply[Int => Int] { x => x + y }
        """
      .contains:
        """
        Invalid capture of variable `y`. Use the first parameter of a spore's body to refer to the spore's environment.
        """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        Spore.apply[Int => Int] { x => Spore.apply[Int => Int] { y => x + y }.unwrap().apply(x) }
        """
      .contains:
        """
        Invalid capture of variable `x`. Use the first parameter of a spore's body to refer to the spore's environment.
        """.trim()

  @Test
  def testInvalidCaptureMethodParameter(): Unit =
    assertTrue:
      typeCheckErrorMessages:
        """
        def fun(x: Int): Spore[Int => Boolean] = Spore.apply[Int => Boolean] { y => y > x }
        """
      .contains:
        """
        Invalid capture of variable `x`. Use the first parameter of a spore's body to refer to the spore's environment.
        """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        object ShouldFail:
          def fun(x: Int): Spore[Int => Boolean] = Spore.apply[Int => Boolean] { y => y > x }
        """
      .contains:
        """
        Invalid capture of variable `x`. Use the first parameter of a spore's body to refer to the spore's environment.
        """.trim()

  val captureMeIfYouCan = 12

  @Test
  def testInvalidCaptureThis(): Unit =
    assertTrue:
      typeCheckErrorMessages:
        """
        class TestClass {
          Spore.apply { () => this.toString() }.unwrap()
        }
        (new TestClass())
        """
      .contains:
        """
        Invalid capture of `this` from outer class.
        """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        class Outer:
          val x = 12
          Spore.apply { () => 42 * x }.unwrap()
        (new Outer())
        """
      .contains:
        """
        Invalid capture of `this` from class Outer.
        """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        Spore.apply { () => 42 * captureMeIfYouCan }.unwrap()
        """
      .contains:
        """
        Invalid capture of `this` from class SporeLambdaErrorTests.
        """.trim()
