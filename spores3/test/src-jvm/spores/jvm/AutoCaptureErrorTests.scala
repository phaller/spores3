package spores.jvm

import utest._

import upickle.default.*

import spores.default.*
import spores.default.given
import spores.TestUtils.*


object AutoCaptureErrorTestsDefs {

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
  //     def foo = Spore(*) { (x: Int) => x + outer.y }
  //   }
  // }
}


object AutoCaptureErrorTests extends TestSuite {
  import AutoCaptureErrorTestsDefs.*

  val tests = Tests {
    test("testCaptureIdentError") {
      assert:
        typeCheckErrorMessages:
          """
          val foo = Foo(12, 13)
          Spore(*) { foo }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
          val foo = Foo(12, 13)
          Spore(*) { (x: Int) => x + foo.x + foo.y }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
          val foo = Foo(12, 13)
          Spore(*) { def bar(x: Int): Int = { x + foo.x } }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCaptureClassError") {
      assert:
        typeCheckErrorMessages:
          """
          class A(val a: Int)
          Spore(*) { new A(12) }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `A`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
          class A(val a: Int)
          Spore(*) { (x: Int) => x + new A(12).a }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `A`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCaptureMethodError") {
      assert:
        typeCheckErrorMessages:
          """
          def captureMeIfYouCan(): Int = 12
          Spore(*) { (x: Int) => x + captureMeIfYouCan() }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `captureMeIfYouCan`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCaptureThisError") {
      assert:
        typeCheckErrorMessages:
          """
          class Outer {
            Spore(*) { this }
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Outer`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
          class Outer {
            val captureThisXIfYouCan = 99
            Spore(*) { this.captureThisXIfYouCan }
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Outer`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
          class Outer {
            val captureThisXIfYouCan = 99
            Spore(*) { (x: Int) => x + captureThisXIfYouCan }
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Outer`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCaptureImplicitThisError") {
      assert:
        typeCheckErrorMessages:
          """
          case class Bar(x: Int, y: Int)
          given ReadWriter[Bar] = macroRW[Bar]
          given Spore[ReadWriter[Bar]] = Spore(*) { summon[ReadWriter[Bar]] }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `given_ReadWriter_Bar`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCaptureOpaqueTypeError") {
      assert:
        typeCheckErrorMessages:
          """
          val opaqueInt = OpaqueInt(12)
          Spore(*) { (x: Int) => x + OpaqueInt.unwrap(opaqueInt) }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `opaqueInt`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCapturedThisNestedClassError") {
      assert:
        typeCheckErrorMessages:
          """
          class Outer {
            class Inner {
              val y = 12
              def foo = Spore(*) { (x: Int) => x + y }
            }
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Inner`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
          class Outer {
            val y = 12
            class Inner {
              def foo = Spore(*) { (x: Int) => x + y }
            }
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Outer`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
          class Outer {
            val y = 12
            class Inner {
              def foo = Spore(*) { (x: Int) => x + Outer.this.y }
            }
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Outer`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCapturedIdentInClassError") {
      assert:
        typeCheckErrorMessages:
          """
          val foo = Foo(12, 13)
          Spore(*) {
            class Bar {
              def bar = foo.x + 12
            }
          }.get()
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `foo`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCapturedNewClassError") {
      assert:
        typeCheckErrorMessages:
          """
          class Bar(x: Int, y: Int)
          Spore(*) { (x: Int) =>
            new Bar(12, 14)
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
          class Bar[T](x: T, y: T)
          Spore(*) { (x: Int) =>
            new Bar[Int](12, 14)
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCapturedClassExtendsError") {
      assert:
        typeCheckErrorMessages:
          """
            class Bar0
            Spore(*) {
              class FooBar extends Bar0
            }.get()
            """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar0`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
            class Bar1(x: Int, y: Int)
            Spore(*) {
              class FooBar extends Bar1(12, 13)
            }
            """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar1`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
            class Bar2[T](x: T, y: T)
            Spore(*) {
              class FooBar extends Bar2[Int](12, 13)
            }.get()
            """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar2`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
            val x = 12
            trait Bar3 { def bar: Int = x }
            Spore(*) {
              class FooBar extends Foo(12, 13) with Bar3
            }
            """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar3`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()

      assert:
        typeCheckErrorMessages:
          """
            trait Bar4[T] { def bar: Int = x }
            Spore(*) {
              class FooBar extends Foo(12, 13) with Bar4[Int]
            }.get()
            """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar4`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCapturedTraitExtendsError") {
      assert:
        typeCheckErrorMessages:
          """
          trait Bar
          Spore(*) {
            trait FooBar extends Bar
          }.get()
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testObjectExtendsCapturedError") {
      assert:
        typeCheckErrorMessages:
          """
          trait Bar
          Spore(*) {
            object FooBar extends Bar
          }.get()
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCapturedEnumError") {
      assert:
        typeCheckErrorMessages:
          """
          enum Bar { case Baz }
          Spore(*) { Bar.Baz }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCapturedUnapplyError") {
      assert:
        typeCheckErrorMessages:
          """
          sealed trait Bar
          case class Baz(x: Int, y: Int) extends Bar
          Spore(*) { (x: Bar) => x match {
              case Baz(a, b) => a + b
            }
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Baz`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }

    test("testCapturedThisSuperError") {
      assert:
        typeCheckErrorMessages:
          """
          class Bar extends Foo(12, 13) {
            def bar = Spore(*) { (x: Int) => x.toString() + super.toString() }
          }
          """
        .exists:
          _.matches:
            raw"""
            (?s)Missing implicit for captured variable `Bar`\.\R\Rno implicit values were found that match type spores.Spore0?\[.*upickle\.default\.ReadWriter\[.*\].*\]\s*
            """.trim()
    }
  }
}
