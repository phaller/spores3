package spores.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import upickle.default.*

import spores.default.*
import spores.default.given
import spores.TestUtils.*


object AutoCaptureErrorTests {

  /** Foo values cannot be captured as there is no Spore[ReadWriter[Foo]]]. */
  case class Foo(x: Int, y: Int)

  /** Opaque type without a ReadWriter. */
  opaque type OpaqueInt = Int
  object OpaqueInt {
    def apply(value: Int): OpaqueInt = value
    def unwrap(value: OpaqueInt): Int = value
  }

  // For some reason this doesn't cause any errors when using the
  // `typeCheckErrorMessages` method, but it does so here...
  // class Outer { outer =>
  //   val y = 12
  //   class Inner {
  //     def foo = Spore.auto { (x: Int) => x + outer.y }
  //   }
  // }
}


@RunWith(classOf[JUnit4])
class AutoCaptureErrorTests {
  import AutoCaptureErrorTests.*

  @Test
  def testCaptureIdentError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        val foo = Foo(12, 13)
        Spore.auto { foo }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        val foo = Foo(12, 13)
        Spore.auto { (x: Int) => x + foo.x + foo.y }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        val foo = Foo(12, 13)
        Spore.auto { def bar(x: Int): Int = { x + foo.x } }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCaptureClassError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        class A(val a: Int)
        Spore.auto { new A(12) }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `A`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        class A(val a: Int)
        Spore.auto { (x: Int) => x + new A(12).a }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `A`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCaptureMethodError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        def captureMeIfYouCan(): Int = 12
        Spore.auto { (x: Int) => x + captureMeIfYouCan() }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `captureMeIfYouCan`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  val captureThisXIfYouCan = 99

  @Test
  def testCaptureThisError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        Spore.auto { this }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `AutoCaptureErrorTests`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        Spore.auto { this.captureThisXIfYouCan }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `AutoCaptureErrorTests`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        Spore.auto { (x: Int) => x + captureThisXIfYouCan }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `AutoCaptureErrorTests`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCaptureImplicitThisError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        case class Bar(x: Int, y: Int)
        given ReadWriter[Bar] = macroRW[Bar]
        given Spore[ReadWriter[Bar]] = Spore.auto { summon[ReadWriter[Bar]] }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `given_ReadWriter_Bar`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCaptureOpaqueTypeError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        val opaqueInt = OpaqueInt(12)
        Spore.auto { (x: Int) => x + OpaqueInt.unwrap(opaqueInt) }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `opaqueInt`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedThisNestedClassError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        class Outer {
          class Inner {
            val y = 12
            def foo = Spore.auto { (x: Int) => x + y }
          }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Inner`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        class Outer {
          val y = 12
          class Inner {
            def foo = Spore.auto { (x: Int) => x + y }
          }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Outer`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        class Outer {
          val y = 12
          class Inner {
            def foo = Spore.auto { (x: Int) => x + Outer.this.y }
          }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Outer`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedIdentInClassError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        val foo = Foo(12, 13)
        Spore.auto {
          class Bar {
            def bar = foo.x + 12
          }
        }.unwrap()
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedNewClassError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        class Bar(x: Int, y: Int)
        Spore.auto { (x: Int) =>
          new Bar(12, 14)
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        class Bar[T](x: T, y: T)
        Spore.auto { (x: Int) =>
          new Bar[Int](12, 14)
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedClassExtendsError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
          class Bar0
          Spore.auto {
            class FooBar extends Bar0
          }.unwrap()
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar0`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
          class Bar1(x: Int, y: Int)
          Spore.auto {
            class FooBar extends Bar1(12, 13)
          }
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar1`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
          class Bar2[T](x: T, y: T)
          Spore.auto {
            class FooBar extends Bar2[Int](12, 13)
          }.unwrap()
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar2`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
          val x = 12
          trait Bar3 { def bar: Int = x }
          Spore.auto {
            class FooBar extends Foo(12, 13) with Bar3
          }
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar3`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
          trait Bar4[T] { def bar: Int = x }
          Spore.auto {
            class FooBar extends Foo(12, 13) with Bar4[Int]
          }.unwrap()
          """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar4`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedTraitExtendsError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        trait Bar
        Spore.auto {
          trait FooBar extends Bar
        }.unwrap()
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testObjectExtendsCapturedError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        trait Bar
        Spore.auto {
          object FooBar extends Bar
        }.unwrap()
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedEnumError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        enum Bar { case Baz }
        Spore.auto { Bar.Baz }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedUnapplyError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        sealed trait Bar
        case class Baz(x: Int, y: Int) extends Bar
        Spore.auto { (x: Bar) => x match {
            case Baz(a, b) => a + b
          }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Baz`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }

  @Test
  def testCapturedThisSuperError(): Unit = {
    assertTrue:
      typeCheckErrorMessages:
        """
        class Bar extends Foo(12, 13) {
          def bar = Spore.auto { (x: Int) => x.toString() + super.toString() }
        }
        """
      .exists:
        _.matches:
          raw"""
          (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore\[\s*upickle.default.ReadWriter\[.*\]\]\s*
          """.trim()
  }
}
