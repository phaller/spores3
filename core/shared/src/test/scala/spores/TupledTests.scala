package spores

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import spores.default.*
import spores.default.given

object TupledTests {

  object SporeFunUntupled0 extends SporeBuilder[Function0[Int]]({ () => 1 })
  object SporeFunUntupled1 extends SporeBuilder[Function1[Int, Int]]({ (t1) => t1 + 1 })
  object SporeFunUntupled2 extends SporeBuilder[Function2[String, String, String]]({ (t1, t2) => t1 + t2 })

  class Foo(x: Int) {
    override def toString(): String = s"Foo($x)"
  }
  object SporeFunUntupled3 extends SporeBuilder[Function3[Foo, Int, String, String]]({ (t1, t2, t3) => t1.toString() + t2.toString() + t3 })

  object SporeFunUntupled4 extends SporeBuilder[Function4[Int, Int, Int, Int, Int]]({ (t1, t2, t3, t4) => t1 + t2 + t3 + t4 })
  object SporeFunUntupled5 extends SporeBuilder[Function5[Int, Int, Int, Int, Int, Int]]({ (t1, t2, t3, t4, t5) => t1 + t2 + t3 + t4 + t5 })
  object SporeFunUntupled6 extends SporeBuilder[Function6[Int, Int, Int, Int, Int, Int, Int]]({ (t1, t2, t3, t4, t5, t6) => t1 + t2 + t3 + t4 + t5 + t6 })
  object SporeFunUntupled7 extends SporeBuilder[Function7[Int, Int, Int, Int, Int, Int, Int, Int]]({ (t1, t2, t3, t4, t5, t6, t7) => t1 + t2 + t3 + t4 + t5 + t6 + t7 })

  object SporeFunTupled0 extends SporeBuilder[Function1[EmptyTuple, Int]]({ _ => 1 })
  object SporeFunTupled1 extends SporeBuilder[Function1[Tuple1[Int], Int]]({ case Tuple1(x1) => x1 + 1 })
  object SporeFunTupled2 extends SporeBuilder[Function1[(String, String), String]]({ case (x1, x2) => x1 + x2 })
  object SporeFunTupled3 extends SporeBuilder[Function1[(Foo, Int, String), String]]({ case (x1, x2, x3) => x1.toString() + x2.toString() + x3 })
  object SporeFunTupled4 extends SporeBuilder[Function1[(Int, Int, Int, Int), Int]]({ case (x1, x2, x3, x4) => x1 + x2 + x3 + x4 })
  object SporeFunTupled5 extends SporeBuilder[Function1[(Int, Int, Int, Int, Int), Int]]({ case (x1, x2, x3, x4, x5) => x1 + x2 + x3 + x4 + x5 })
  object SporeFunTupled6 extends SporeBuilder[Function1[(Int, Int, Int, Int, Int, Int), Int]]({ case (x1, x2, x3, x4, x5, x6) => x1 + x2 + x3 + x4 + x5 + x6 })
  object SporeFunTupled7 extends SporeBuilder[Function1[(Int, Int, Int, Int, Int, Int, Int), Int]]({ case (x1, x2, x3, x4, x5, x6, x7) => x1 + x2 + x3 + x4 + x5 + x6 + x7 })
}

@RunWith(classOf[JUnit4])
class TupledTests {
  import TupledTests.*

  @Test
  def testTupled(): Unit = {
    val f0 = SporeFunUntupled0.build().tupled0
    val f1 = SporeFunUntupled1.build().tupled1
    val f2 = SporeFunUntupled2.build().tupled2
    val f3 = SporeFunUntupled3.build().tupled3
    val f4 = SporeFunUntupled4.build().tupled4
    val f5 = SporeFunUntupled5.build().tupled5
    val f6 = SporeFunUntupled6.build().tupled6
    val f7 = SporeFunUntupled7.build().tupled7

    assertEquals(1, f0(EmptyTuple))
    assertEquals(2, f1(Tuple1(1)))
    assertEquals("HelloWorld", f2(("Hello", "World")))
    assertEquals("Foo(1)12", f3(Tuple3(new Foo(1), 1, "2")))
    assertEquals(10, f4((1, 2, 3, 4)))
    assertEquals(15, f5((1, 2, 3, 4, 5)))
    assertEquals(21, f6((1, 2, 3, 4, 5, 6)))
    assertEquals(28, f7((1, 2, 3, 4, 5, 6, 7)))
  }

  @Test
  def testUntupled(): Unit = {
    val f0 = SporeFunTupled0.build().untupled0
    val f1 = SporeFunTupled1.build().untupled1
    val f2 = SporeFunTupled2.build().untupled2
    val f3 = SporeFunTupled3.build().untupled3
    val f4 = SporeFunTupled4.build().untupled4
    val f5 = SporeFunTupled5.build().untupled5
    val f6 = SporeFunTupled6.build().untupled6
    val f7 = SporeFunTupled7.build().untupled7

    assertEquals(1, f0())
    assertEquals(2, f1(1))
    assertEquals("HelloWorld", f2("Hello", "World"))
    assertEquals("Foo(1)12", f3(new Foo(1), 1, "2"))
    assertEquals(10, f4(1, 2, 3, 4))
    assertEquals(15, f5(1, 2, 3, 4, 5))
    assertEquals(21, f6(1, 2, 3, 4, 5, 6))
    assertEquals(28, f7(1, 2, 3, 4, 5, 6, 7))
  }
}
