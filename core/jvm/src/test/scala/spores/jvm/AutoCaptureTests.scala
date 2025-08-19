package spores.jvm

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import upickle.default.*

import spores.*
import spores.given
import spores.TestUtils.*


object AutoCaptureTests {

  case class Foo(x: Int, y: Int)

  def writeReadUnwrap[T](s: Spore[T]): T = {
    val w = write(s)
    val r = read[Spore[T]](w)
    r.unwrap()
  }

  def readUnwrap[T](json: String): T = {
    val r = read[Spore[T]](json)
    r.unwrap()
  }

  def countCapturedInSpore[T](s: Spore[T], captured: String): Int = {
    // Note: the `captured` String should otherwise not occur in the JSON.
    val length = captured.length
    val json = write(s)
    json.sliding(length).count(_.contains(captured))
  }

  object FunctionsToReadFromJSON {
    def fun0() = Spore.auto { (x: String) => x }
    def fun1(y1: String) = Spore.auto { (x: String) => x + y1 }
    def fun2(y1: String, y2: String) = Spore.auto { (x: String) => x + y1 + y2 }
    def fun3(y1: String, y2: String, y3: List[String]) = Spore.auto { (x: String) => x + y1 + y2 + y3.mkString(",") }
  }

  sealed trait Tree[T] derives ReadWriter
  case class Leaf[T](value: T) extends Tree[T] derives ReadWriter
  case class Node[T](left: Tree[T], right: Tree[T]) extends Tree[T] derives ReadWriter
  def reduce[T](tree: Tree[T], f: (T, T) => T): T = {
    tree match {
      case Leaf(value) => value
      case Node(left, right) => f(reduce(left, f), reduce(right, f))
    }
  }
  object TreeRW extends SporeBuilder[ReadWriter[Tree[Int]]]({ macroRW })
  object LeafRW extends SporeBuilder[ReadWriter[Leaf[Int]]]({ macroRW })
  object NodeRW extends SporeBuilder[ReadWriter[Node[Int]]]({ macroRW })
  given treeRW: Spore[ReadWriter[Tree[Int]]] = TreeRW.build()
  given leafRW: Spore[ReadWriter[Leaf[Int]]] = LeafRW.build()
  given nodeRW: Spore[ReadWriter[Node[Int]]] = NodeRW.build()

  class TopLevel {
    def x: Int = 4
  }

  def from[T](x: T): List[T] = List(x)
}


@RunWith(classOf[JUnit4])
class AutoCaptureTests {
  import AutoCaptureTests.*

  @Test
  def testCaptureNothing(): Unit = {
    val fun = Spore.auto {}
    val unwrapped = writeReadUnwrap(fun)
    assertEquals((), unwrapped)
  }

  @Test
  def testCaptures01234(): Unit = {
    val a1 = "0123456789-1"
    val a2 = "0123456789-2"
    val a3 = "0123456789-3"
    val a4 = "0123456789-4"

    val fun0 = Spore.auto { (x: String) => x }
    val fun1 = Spore.auto { (x: String) => x + a1 }
    val fun2 = Spore.auto { (x: String) => x + a1 + a2 }
    val fun3 = Spore.auto { (x: String) => x + a1 + a2 + a3 }
    val fun4 = Spore.auto { (x: String) => x + a1 + a2 + a3 + a4 }

    val unwrapped0 = writeReadUnwrap(fun0)
    val unwrapped1 = writeReadUnwrap(fun1)
    val unwrapped2 = writeReadUnwrap(fun2)
    val unwrapped3 = writeReadUnwrap(fun3)
    val unwrapped4 = writeReadUnwrap(fun4)
    
    assertEquals("hello", unwrapped0("hello"))
    assertEquals("hello" + a1, unwrapped1("hello"))
    assertEquals("hello" + a1 + a2, unwrapped2("hello"))
    assertEquals("hello" + a1 + a2 + a3, unwrapped3("hello"))
    assertEquals("hello" + a1 + a2 + a3 + a4, unwrapped4("hello"))

    assertEquals(1, countCapturedInSpore(fun1, a1))
    assertEquals(1, countCapturedInSpore(fun2, a1))
    assertEquals(1, countCapturedInSpore(fun2, a2))
    assertEquals(1, countCapturedInSpore(fun3, a1))
    assertEquals(1, countCapturedInSpore(fun3, a2))
    assertEquals(1, countCapturedInSpore(fun3, a3))
    assertEquals(1, countCapturedInSpore(fun4, a1))
    assertEquals(1, countCapturedInSpore(fun4, a2))
    assertEquals(1, countCapturedInSpore(fun4, a3))
    assertEquals(1, countCapturedInSpore(fun4, a4))
  }

  @Test
  def testCapturedIdentExactlyOnce(): Unit = {
    val c = "0123456789"
    val fun = Spore.auto { (x: String) =>
      val y = {
        val z = {
          x + c
        }
        z + c
      }
      y + c + c
    }
    val unwrapped = writeReadUnwrap(fun)
    assertEquals("0123456789" + c + c + c + c, unwrapped("0123456789"))
    assertEquals(1, countCapturedInSpore(fun, c))
  }

  @Test
  def testReadFun0123(): Unit = {
    val json0 = """{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.AutoCaptureTests$FunctionsToReadFromJSON$Lambda$16"}"""
    val json1 = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.AutoCaptureTests$FunctionsToReadFromJSON$Lambda$17"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"\"0123456789-1\"","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$StringRW$"}}}"""
    val json2 = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.AutoCaptureTests$FunctionsToReadFromJSON$Lambda$18"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"\"0123456789-1\"","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$StringRW$"}}},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"\"0123456789-2\"","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$StringRW$"}}}"""
    val json3 = """{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.jvm.AutoCaptureTests$FunctionsToReadFromJSON$Lambda$19"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"\"0123456789-1\"","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$StringRW$"}}},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"\"0123456789-2\"","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$StringRW$"}}},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"[\"a\",\"b\",\"c\"]","rw":{"$type":"spores.Packed.PackedWithCtx","packed":{"$type":"spores.Packed.PackedClass","className":"spores.ReadWriters$ListRW"},"packedEnv":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$StringRW$"}}}}"""

    val a1 = "0123456789-1"
    val a2 = "0123456789-2"
    val a3 = List("a", "b", "c")

    val u0 = readUnwrap[String => String](json0)
    val u1 = readUnwrap[String => String](json1)
    val u2 = readUnwrap[String => String](json2)
    val u3 = readUnwrap[String => String](json3)

    assertEquals("hello", u0("hello"))
    assertEquals("hello" + a1, u1("hello"))
    assertEquals("hello" + a1 + a2, u2("hello"))
    assertEquals("hello" + a1 + a2 + a3.mkString(","), u3("hello"))
  }

  @Test
  def testCaptureTree(): Unit = {
    val tree = Node(Leaf(1), Node(Leaf(2), Leaf(3)))
    val fun = Spore.auto { reduce(tree, (x, y) => x + y) }
    val unwrapped = writeReadUnwrap(fun)
    assertEquals(6, unwrapped)
  }

  @Test
  def testValDefNoCapture(): Unit = {
    val fun = Spore.auto { (x: Int) =>
      val y = 12
      x + y
    }
    val unwrapped = writeReadUnwrap(fun)
    assertEquals(13, unwrapped(1))
  }

  @Test
  def testTypeDefNoCapture(): Unit = {
    val fun = Spore.auto { (y: Int) =>
      type T = TopLevel
      new T { override def x = y }
    }
    val unwrapped = writeReadUnwrap(fun)
    assertEquals(13, unwrapped(13).x)
  }

  @Test
  def testAsInstanceOfNoCapture(): Unit = {
    class Bar(val value: Int)
    val fun = Spore.auto { (x: List[Any]) =>
      x.asInstanceOf[List[Bar]].tail.head.value + x.head.asInstanceOf[Bar].value
    }
    val unwrapped = writeReadUnwrap(fun)
    assertEquals(56, unwrapped(List(Bar(42), Bar(14), Bar(73))))
  }

  @Test
  def testMethodTypeParamNoCapture(): Unit = {
    class Bar(val value: Int)
    val fun = Spore.auto { (x: Any) =>
      from[Bar](x.asInstanceOf[Bar]).head.value
    }
    val unwrapped = writeReadUnwrap(fun)
    assertEquals(39, unwrapped.apply(Bar(39)))
  }

  @Test
  def testParameterTypeNoCapture(): Unit = {
    class Bar(val value: Int)
    val fun = Spore.auto { 
      (l: List[Bar]) 
        => (x: Bar) =>
          val foo: Bar = null
          l.head.value + x.value
    }
    val unwrapped = writeReadUnwrap(fun)
    assertEquals(76, unwrapped(List(Bar(36), Bar(31)))(Bar(40)))
  }

  @Test
  def testClassExtendsTopLevelNoCapture(): Unit = {
    val fun = Spore.auto {
      class FooBar extends Foo(12, 13) {
        def foo: Foo = this
      }
      val fooBar = new FooBar()
      fooBar.foo.x
    }
    val unwrapped = writeReadUnwrap(fun)
    assertEquals(12, unwrapped)
  }

  @Test
  def testLargeProgram(): Unit = {
    val C00 = 0; val C01 = 1; val C02 = 2; val C03 = 3; val C04 = 4; val C05 = 5; val C06 = 6; val C07 = 7; val C08 = 8; val C09 = 9;
    val C10 = 0; val C11 = 1; val C12 = 2; val C13 = 3; val C14 = 4; val C15 = 5; val C16 = 6; val C17 = 7; val C18 = 8; val C19 = 9;
    val C20 = 0; val C21 = 1; val C22 = 2; val C23 = 3; val C24 = 4; val C25 = 5; val C26 = 6; val C27 = 7; val C28 = 8; val C29 = 9;
    val C30 = 0; val C31 = 1; val C32 = 2; val C33 = 3; val C34 = 4; val C35 = 5; val C36 = 6; val C37 = 7; val C38 = 8; val C39 = 9;
    val C40 = 0; val C41 = 1; val C42 = 2; val C43 = 3; val C44 = 4; val C45 = 5; val C46 = 6; val C47 = 7; val C48 = 8; val C49 = 9;

    val fun = Spore.auto { 
      (x00: Int) => (x01: Int) => (x02: Int) => (x03: Int) => (x04: Int) => (x05: Int) => (x06: Int) => (x07: Int) => (x08: Int) => (x09: Int) =>
      (x10: Int) => (x11: Int) => (x12: Int) => (x13: Int) => (x14: Int) => (x15: Int) => (x16: Int) => (x17: Int) => (x18: Int) => (x19: Int) =>
      (x20: Int) => (x21: Int) => (x22: Int) => (x23: Int) => (x24: Int) => (x25: Int) => (x26: Int) => (x27: Int) => (x28: Int) => (x29: Int) =>
      (x30: Int) => (x31: Int) => (x32: Int) => (x33: Int) => (x34: Int) => (x35: Int) => (x36: Int) => (x37: Int) => (x38: Int) => (x39: Int) =>
      (x40: Int) => (x41: Int) => (x42: Int) => (x43: Int) => (x44: Int) => (x45: Int) => (x46: Int) => (x47: Int) => (x48: Int) => (x49: Int) =>
        x00 + x01 + x02 + x03 + x04 + x05 + x06 + x07 + x08 + x09 +
        x10 + x11 + x12 + x13 + x14 + x15 + x16 + x17 + x18 + x19 +
        x20 + x21 + x22 + x23 + x24 + x25 + x26 + x27 + x28 + x29 +
        x30 + x31 + x32 + x33 + x34 + x35 + x36 + x37 + x38 + x39 +
        x40 + x41 + x42 + x43 + x44 + x45 + x46 + x47 + x48 + x49 +
        C00 + C01 + C02 + C03 + C04 + C05 + C06 + C07 + C08 + C09 +
        C10 + C11 + C12 + C13 + C14 + C15 + C16 + C17 + C18 + C19 +
        C20 + C21 + C22 + C23 + C24 + C25 + C26 + C27 + C28 + C29 +
        C30 + C31 + C32 + C33 + C34 + C35 + C36 + C37 + C38 + C39 +
        C40 + C41 + C42 + C43 + C44 + C45 + C46 + C47 + C48 + C49
    }

    val unwrapped = writeReadUnwrap(fun)
    val actual = unwrapped
      .apply(C00).apply(C01).apply(C02).apply(C03).apply(C04).apply(C05).apply(C06).apply(C07).apply(C08).apply(C09)
      .apply(C10).apply(C11).apply(C12).apply(C13).apply(C14).apply(C15).apply(C16).apply(C17).apply(C18).apply(C19)
      .apply(C20).apply(C21).apply(C22).apply(C23).apply(C24).apply(C25).apply(C26).apply(C27).apply(C28).apply(C29)
      .apply(C30).apply(C31).apply(C32).apply(C33).apply(C34).apply(C35).apply(C36).apply(C37).apply(C38).apply(C39)
      .apply(C40).apply(C41).apply(C42).apply(C43).apply(C44).apply(C45).apply(C46).apply(C47).apply(C48).apply(C49)
    val expected = 450 // (0 + 1 + ... + 9) * 5 * 2
    assertEquals(expected, actual)
  }
}
