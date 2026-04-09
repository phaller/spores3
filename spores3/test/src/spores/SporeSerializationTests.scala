package spores

import utest._
import upickle.default.*

import spores.default.*
import spores.default.given


object SporeSerializationTestsDefs {

  object IntSpore extends SporeBuilder[Int] {
    override def body = 12
  }
  object StringSpore extends SporeBuilder[String] {
    override def body = "Hello"
  }
  object BooleanSpore extends SporeBuilder[Boolean] {
    override def body = true
  }
  object DoubleSpore extends SporeBuilder[Double] {
    override def body = 0.5
  }
  object FloatSpore extends SporeBuilder[Float] {
    override def body = 1.5f
  }
  object LongSpore extends SporeBuilder[Long] {
    override def body = 987654321L
  }
  object ShortSpore extends SporeBuilder[Short] {
    override def body = 12.toShort
  }
  object ByteSpore extends SporeBuilder[Byte] {
    override def body = 7.toByte
  }
  object CharSpore extends SporeBuilder[Char] {
    override def body = 'a'
  }
  object UnitSpore extends SporeBuilder[Unit] {
    override def body = ()
  }

  object IntToIntSpore extends SporeBuilder[Int => Int] {
    override def body = x => x + 12
  }
  object IntToStringSpore extends SporeBuilder[Int => String] {
    override def body = x => x.toString()
  }
  object StringToIntSpore extends SporeBuilder[String => Int] {
    override def body = s => s.length
  }
  object IntPredicateSpore extends SporeBuilder[Int => Boolean] {
    override def body = x => x > 10
  }
  object IntCtxToStringSpore extends SporeBuilder[Int ?=> String] {
    override def body = summon[Int].toString()
  }
  object IntToSporeStringSpore extends SporeBuilder[Int => Spore[String]] {
    override def body = x => Spore.value(x.toString())
  }

  object IntIntToIntSpore extends SporeBuilder[(Int, Int) => Int] {
    override def body = (x, y) => x + y + 12
  }
  object IntIntToStringSpore extends SporeBuilder[(Int, Int) => String] {
    override def body = (x, y) => (x + y).toString()
  }

  class Identity[T] extends SporeClassBuilder[T => T] {
    override def body = x => x
  }
  class IntToStringClassSpore extends SporeClassBuilder[Int => String] {
    override def body = x => x.toString()
  }
  class IntCtxToStringClassSpore extends SporeClassBuilder[Int ?=> String] {
    override def body = summon[Int].toString()
  }
  class TToStringClassSpore[T] extends SporeClassBuilder[T => String] {
    override def body = x => x.toString()
  }
  class TToTClassSpore[T] extends SporeClassBuilder[T => (T => T) => T] {
    override def body = t => f => f(t)
  }
  object SporeToString extends SporeBuilder[Spore[Int => Boolean] => Int => String] {
    override def body = env => x => if env.get()(x) then "yes" else "no"
  }
  object IntToStringCurried extends SporeBuilder[Int => Int => String] {
    override def body = x => y => (x + y).toString()
  }

  case class Foo(x: Int, y: String)
  object FooRW extends SporeBuilder[ReadWriter[Foo]] {
    override def body = { macroRW }
  }
  given Spore[ReadWriter[Foo]] = FooRW.build()

  object FooToStringSpore extends SporeBuilder[Foo => String] {
    override def body = p => "x: " + p.x.toString() + ", y: " + p.y
  }

  val intEnv: Spore[Int] = Spore.value(12)
  val stringEnv: Spore[String] = Spore.value("Hello")
  val booleanEnv: Spore[Boolean] = Spore.value(true)
  val doubleEnv: Spore[Double] = Spore.value(0.5)
  val floatEnv: Spore[Float] = Spore.value(1.5f)
  val longEnv: Spore[Long] = Spore.value(987654321L)
  val shortEnv: Spore[Short] = Spore.value(12.toShort)
  val byteEnv: Spore[Byte] = Spore.value(7.toByte)
  val charEnv: Spore[Char] = Spore.value('a')
  val unitEnv: Spore[Unit] = Spore.value(())
  val sporeIntEnv: Spore[Spore[Int]] = Spore.value(IntSpore.build())
  val fooEnv: Spore[Foo] = Spore.value(Foo(1, "337"))
  val someIntEnv: Spore[Some[Int]] = Spore.value(Some(12))
  val noneEnv: Spore[None.type] = Spore.value(None)
  val listIntEnv: Spore[List[Int]] = Spore.value(List(1, 2, 3))
  val tuple1Env: Spore[Tuple1[Int]] = Spore.value(Tuple1(12))
  val tuple2Env: Spore[(Int, String)] = Spore.value((1, "a"))
  val tuple3Env: Spore[(Int, String, Boolean)] = Spore.value((1, "a", true))
  val eitherRightEnv: Spore[Either[String, Int]] = Spore.value(Right(12): Either[String, Int])
  val eitherLeftEnv: Spore[Either[String, Int]] = Spore.value(Left("err"): Either[String, Int])
  val nestedSporeEnv: Spore[Spore[Int => Boolean]] = Spore.value(IntPredicateSpore.build())
}

object SporeSerializationTestsDeadCode {

  object IntSpore extends SporeBuilder[Int] {
    override def body = 12
  }
  object StringSpore extends SporeBuilder[String] {
    override def body = "Hello"
  }
  object BooleanSpore extends SporeBuilder[Boolean] {
    override def body = true
  }
  object DoubleSpore extends SporeBuilder[Double] {
    override def body = 0.5
  }
  object FloatSpore extends SporeBuilder[Float] {
    override def body = 1.5f
  }
  object LongSpore extends SporeBuilder[Long] {
    override def body = 987654321L
  }
  object ShortSpore extends SporeBuilder[Short] {
    override def body = 12.toShort
  }
  object ByteSpore extends SporeBuilder[Byte] {
    override def body = 7.toByte
  }
  object CharSpore extends SporeBuilder[Char] {
    override def body = 'a'
  }
  object UnitSpore extends SporeBuilder[Unit] {
    override def body = ()
  }

  object IntToIntSpore extends SporeBuilder[Int => Int] {
    override def body = x => x + 12
  }
  object IntToStringSpore extends SporeBuilder[Int => String] {
    override def body = x => x.toString()
  }
  object StringToIntSpore extends SporeBuilder[String => Int] {
    override def body = s => s.length
  }
  object IntPredicateSpore extends SporeBuilder[Int => Boolean] {
    override def body = x => x > 10
  }
  object IntCtxToStringSpore extends SporeBuilder[Int ?=> String] {
    override def body = summon[Int].toString()
  }
  object IntToSporeStringSpore extends SporeBuilder[Int => Spore[String]] {
    override def body = x => Spore.value(x.toString())
  }

  object IntIntToIntSpore extends SporeBuilder[(Int, Int) => Int] {
    override def body = (x, y) => x + y + 12
  }
  object IntIntToStringSpore extends SporeBuilder[(Int, Int) => String] {
    override def body = (x, y) => (x + y).toString()
  }

  class Identity[T] extends SporeClassBuilder[T => T] {
    override def body = x => x
  }
  class IntToStringClassSpore extends SporeClassBuilder[Int => String] {
    override def body = x => x.toString()
  }
  class IntCtxToStringClassSpore extends SporeClassBuilder[Int ?=> String] {
    override def body = summon[Int].toString()
  }
  class TToStringClassSpore[T] extends SporeClassBuilder[T => String] {
    override def body = x => x.toString()
  }
  class TToTClassSpore[T] extends SporeClassBuilder[T => (T => T) => T] {
    override def body = t => f => f(t)
  }
  object SporeToString extends SporeBuilder[Spore[Int => Boolean] => Int => String] {
    override def body = env => x => if env.get()(x) then "yes" else "no"
  }
  object IntToStringCurried extends SporeBuilder[Int => Int => String] {
    override def body = x => y => (x + y).toString()
  }

  case class Foo(x: Int, y: String)
  object FooRW extends SporeBuilder[ReadWriter[Foo]] {
    override def body = macroRW
  }
  given Spore[ReadWriter[Foo]] = FooRW.build()

  object FooToStringSpore extends SporeBuilder[Foo => String] {
    override def body = p => "x: " + p.x.toString() + ", y: " + p.y
  }
}

object SporeSerializationTests extends TestSuite {
  import SporeSerializationTestsDefs.*

  val tests = Tests {

    test("testSerializeIntSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntSpore$"}"""
      val spore = IntSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int]](json)
      assert(12 == spore2.get())
    }

    test("testSerializeStringSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$StringSpore$"}"""
      val spore = StringSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[String]](json)
      assert("Hello" == spore2.get())
    }

    test("testSerializeBooleanSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$BooleanSpore$"}"""
      val spore = BooleanSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Boolean]](json)
      assert(true == spore2.get())
    }

    test("testSerializeDoubleSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$DoubleSpore$"}"""
      val spore = DoubleSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Double]](json)
      assert(0.5 == spore2.get())
    }

    test("testSerializeFloatSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$FloatSpore$"}"""
      val spore = FloatSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Float]](json)
      assert(1.5f == spore2.get())
    }

    test("testSerializeLongSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$LongSpore$"}"""
      val spore = LongSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Long]](json)
      assert(987654321L == spore2.get())
    }

    test("testSerializeShortSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$ShortSpore$"}"""
      val spore = ShortSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Short]](json)
      assert(12.toShort == spore2.get())
    }

    test("testSerializeByteSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$ByteSpore$"}"""
      val spore = ByteSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Byte]](json)
      assert(7.toByte == spore2.get())
    }

    test("testSerializeCharSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$CharSpore$"}"""
      val spore = CharSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Char]](json)
      assert('a' == spore2.get())
    }

    test("testSerializeUnitSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$UnitSpore$"}"""
      val spore = UnitSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Unit]](json)
      assert(() == spore2.get())
    }

    test("testSerializeIntToIntSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToIntSpore$"}"""
      val spore = IntToIntSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => Int]](json)
      assert(24 == spore2.get()(12))
    }

    test("testSerializeIntToStringSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToStringSpore$"}"""
      val spore = IntToStringSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => String]](json)
      assert("12" == spore2.get()(12))
    }

    test("testSerializeStringToIntSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$StringToIntSpore$"}"""
      val spore = StringToIntSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[String => Int]](json)
      assert(5 == spore2.get()("Hello"))
    }

    test("testSerializeIntPredicateSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntPredicateSpore$"}"""
      val spore = IntPredicateSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => Boolean]](json)
      assert(true == spore2.get()(11))
      assert(false == spore2.get()(9))
    }

    test("testSerializeIntCtxToStringSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntCtxToStringSpore$"}"""
      val spore = IntCtxToStringSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int ?=> String]](json)
      assert("12" == spore2.get()(using 12))
    }

    test("testSerializeIntToSporeStringSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToSporeStringSpore$"}"""
      val spore = IntToSporeStringSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => Spore[String]]](json)
      assert("12" == spore2.get()(12).get())
    }

    test("testSerializeIntIntToIntSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntIntToIntSpore$"}"""
      val spore = IntIntToIntSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[(Int, Int) => Int]](json)
      assert(24 == spore2.get()(5, 7))
    }

    test("testSerializeIntIntToStringSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntIntToStringSpore$"}"""
      val spore = IntIntToStringSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[(Int, Int) => String]](json)
      assert("12" == spore2.get()(5, 7))
    }

    test("testSerializeIntToStringClassSpore") {
      val expected = """{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDefs$IntToStringClassSpore"}"""
      val spore = new IntToStringClassSpore().build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => String]](json)
      assert("12" == spore2.get()(12))
    }

    test("testSerializeIntCtxToStringClassSpore") {
      val expected = """{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDefs$IntCtxToStringClassSpore"}"""
      val spore = new IntCtxToStringClassSpore().build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int ?=> String]](json)
      assert("12" == spore2.get()(using 12))
    }

    test("testSerializeTToStringClassSpore") {
      val expected = """{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDefs$TToStringClassSpore"}"""
      val spore = new TToStringClassSpore[Int]().build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => String]](json)
      assert("12" == spore2.get()(12))
    }

    test("testSerializeTToTClassSpore") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDefs$TToTClassSpore"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":5}},"env":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToIntSpore$"}}"""
      val spore = new TToTClassSpore[Int]().build().withEnv(5).withEnv2(IntToIntSpore.build())
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int]](json)
      assert(17 == spore2.get())
    }

    test("testSerializeIdentity") {
      val expected = """{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDefs$Identity"}"""
      val spore = new Identity[Int]().build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => Int]](json)
      assert(12 == spore2.get()(12))
    }

    test("testSerializeSporeToString") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$SporeToString$"}"""
      val spore = SporeToString.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Spore[Int => Boolean] => Int => String]](json)
      assert("yes" == spore2.get()(IntPredicateSpore.build())(11))
      assert("no" == spore2.get()(IntPredicateSpore.build())(9))
    }

    test("testSerializeIntToStringCurried") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToStringCurried$"}"""
      val spore = IntToStringCurried.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Int => Int => String]](json)
      assert("7" == spore2.get()(3)(4))
    }

    test("testSerializeIntToIntWithEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToIntSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val json = write(IntToIntSpore.build().withEnv(12))
      assert(expected == json)
      val spore2 = read[Spore[Int]](json)
      assert(24 == spore2.get())
    }

    test("testSerializeIntToStringWithEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToStringSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val json = write(IntToStringSpore.build().withEnv(12))
      assert(expected == json)
      val spore2 = read[Spore[String]](json)
      assert("12" == spore2.get())
    }

    test("testSerializeStringToIntWithEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$StringToIntSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$StringRW$"},"value":"Hello"}}"""
      val json = write(StringToIntSpore.build().withEnv("Hello"))
      assert(expected == json)
      val spore2 = read[Spore[Int]](json)
      assert(5 == spore2.get())
    }

    test("testSerializeIntToSporeStringWithEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToSporeStringSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val json = write(IntToSporeStringSpore.build().withEnv(12))
      assert(expected == json)
      val spore2 = read[Spore[Spore[String]]](json)
      assert("12" == spore2.get().get())
    }

    test("testSerializeIntToStringCurriedWithEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToStringCurried$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":3}}"""
      val json = write(IntToStringCurried.build().withEnv(3))
      assert(expected == json)
      val spore2 = read[Spore[Int => String]](json)
      assert("7" == spore2.get()(4))
    }

    test("testSerializeSporeToStringWithEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$SporeToString$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$SporeRW$"},"value":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntPredicateSpore$"}}}"""
      val json = write(SporeToString.build().withEnv(IntPredicateSpore.build()))
      assert(expected == json)
      val spore2 = read[Spore[Int => String]](json)
      assert("yes" == spore2.get()(11))
      assert("no" == spore2.get()(9))
    }

    test("testSerializeIntEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}"""
      val json = write(intEnv)
      assert(expected == json)
      val spore2 = read[Spore[Int]](json)
      assert(12 == spore2.get())
    }

    test("testSerializeStringEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$StringRW$"},"value":"Hello"}"""
      val json = write(stringEnv)
      assert(expected == json)
      val spore2 = read[Spore[String]](json)
      assert("Hello" == spore2.get())
    }

    test("testSerializeBooleanEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$BooleanRW$"},"value":true}"""
      val json = write(booleanEnv)
      assert(expected == json)
      val spore2 = read[Spore[Boolean]](json)
      assert(true == spore2.get())
    }

    test("testSerializeDoubleEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$DoubleRW$"},"value":0.5}"""
      val json = write(doubleEnv)
      assert(expected == json)
      val spore2 = read[Spore[Double]](json)
      assert(0.5 == spore2.get())
    }

    test("testSerializeFloatEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$FloatRW$"},"value":1.5}"""
      val json = write(floatEnv)
      assert(expected == json)
      val spore2 = read[Spore[Float]](json)
      assert(1.5f == spore2.get())
    }

    test("testSerializeLongEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$LongRW$"},"value":987654321}"""
      val json = write(longEnv)
      assert(expected == json)
      val spore2 = read[Spore[Long]](json)
      assert(987654321L == spore2.get())
    }

    test("testSerializeShortEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$ShortRW$"},"value":12}"""
      val json = write(shortEnv)
      assert(expected == json)
      val spore2 = read[Spore[Short]](json)
      assert(12.toShort == spore2.get())
    }

    test("testSerializeByteEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$ByteRW$"},"value":7}"""
      val json = write(byteEnv)
      assert(expected == json)
      val spore2 = read[Spore[Byte]](json)
      assert(7.toByte == spore2.get())
    }

    test("testSerializeCharEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$CharRW$"},"value":"a"}"""
      val json = write(charEnv)
      assert(expected == json)
      val spore2 = read[Spore[Char]](json)
      assert('a' == spore2.get())
    }

    test("testSerializeUnitEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$UnitRW$"},"value":null}"""
      val json = write(unitEnv)
      assert(expected == json)
      val spore2 = read[Spore[Unit]](json)
      assert(() == spore2.get())
    }

    test("testSerializeSporeIntEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$SporeRW$"},"value":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntSpore$"}}"""
      val json = write(sporeIntEnv)
      assert(expected == json)
      val spore2 = read[Spore[Spore[Int]]](json)
      assert(12 == spore2.get().get())
    }

    test("testSerializeFooEnv") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$FooRW$"},"value":{"x":1,"y":"337"}}"""
      val json = write(fooEnv)
      assert(expected == json)
      val spore2 = read[Spore[Foo]](json)
      assert(Foo(1, "337") == spore2.get())
    }

    test("testSerializeOptionSome") {
      val expected = """{"tag":"Val","ev":{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.ReadWriters$SomeRW"},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"}},"value":12}"""
      val json = write(someIntEnv)
      assert(expected == json)
      val spore2 = read[Spore[Some[Int]]](json)
      assert(Some(12) == spore2.get())
    }

    test("testSerializeOptionNone") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$NoneRW$"},"value":null}"""
      val json = write(noneEnv)
      assert(expected == json)
      val spore2 = read[Spore[None.type]](json)
      assert(None == spore2.get())
    }

    test("testSerializeList") {
      val expected = """{"tag":"Val","ev":{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.ReadWriters$ListRW"},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"}},"value":[1,2,3]}"""
      val json = write(listIntEnv)
      assert(expected == json)
      val spore2 = read[Spore[List[Int]]](json)
      assert(List(1, 2, 3) == spore2.get())
    }

    test("testSerializeTuple1") {
      val expected = """{"tag":"Val","ev":{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.ReadWriters$Tuple1RW"},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"}},"value":[12]}"""
      val json = write(tuple1Env)
      assert(expected == json)
      val spore2 = read[Spore[Tuple1[Int]]](json)
      assert(Tuple1(12) == spore2.get())
    }

    test("testSerializeTuple2") {
      val expected = """{"tag":"Val","ev":{"tag":"WithEnv","fun":{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.ReadWriters$Tuple2RW"},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"}},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$StringRW$"}},"value":[1,"a"]}"""
      val json = write(tuple2Env)
      assert(expected == json)
      val spore2 = read[Spore[(Int, String)]](json)
      assert((1, "a") == spore2.get())
    }

    test("testSerializeTuple3") {
      val expected = """{"tag":"Val","ev":{"tag":"WithEnv","fun":{"tag":"WithEnv","fun":{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.ReadWriters$Tuple3RW"},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"}},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$StringRW$"}},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$BooleanRW$"}},"value":[1,"a",true]}"""
      val json = write(tuple3Env)
      assert(expected == json)
      val spore2 = read[Spore[(Int, String, Boolean)]](json)
      assert((1, "a", true) == spore2.get())
    }

    test("testSerializeEitherRight") {
      val expected = """{"tag":"Val","ev":{"tag":"WithEnv","fun":{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.ReadWriters$EitherRW"},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$StringRW$"}},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"}},"value":[1,12]}"""
      val json = write(eitherRightEnv)
      assert(expected == json)
      val spore2 = read[Spore[Either[String, Int]]](json)
      assert(Right(12) == spore2.get())
    }

    test("testSerializeEitherLeft") {
      val expected = """{"tag":"Val","ev":{"tag":"WithEnv","fun":{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.ReadWriters$EitherRW"},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$StringRW$"}},"env":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"}},"value":[0,"err"]}"""
      val json = write(eitherLeftEnv)
      assert(expected == json)
      val spore2 = read[Spore[Either[String, Int]]](json)
      assert(Left("err") == spore2.get())
    }

    test("testSerializeNestedSpore") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$SporeRW$"},"value":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntPredicateSpore$"}}"""
      val json = write(nestedSporeEnv)
      assert(expected == json)
      val spore2 = read[Spore[Spore[Int => Boolean]]](json)
      assert(spore2.get().get()(11))
      assert(!spore2.get().get()(9))
    }

    test("testSerializeWithEnv") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntPredicateSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":11}}"""
      val spore = IntPredicateSpore.build().withEnv(11)
      val json = write(spore)
      assert(expected == json)
      val spore2 = IntPredicateSpore.build().withEnv2(Spore.value(11))
      val json2 = write(spore2)
      assert(expected == json2)
      val spore3 = read[Spore[Boolean]](json)
      assert(true == spore3.get())
      val expectedFalse = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntPredicateSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":9}}"""
      val sporeFalse = IntPredicateSpore.build().withEnv(9)
      val jsonFalse = write(sporeFalse)
      assert(expectedFalse == jsonFalse)
      val spore4 = read[Spore[Boolean]](jsonFalse)
      assert(false == spore4.get())
    }

    test("testSerializeWithCtx") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntCtxToStringSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val spore = IntCtxToStringSpore.build().withCtx(12)
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[String]](json)
      assert("12" == spore2.get())
    }

    test("testSerializeWithCtx2") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntCtxToStringSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val spore = IntCtxToStringSpore.build().withCtx2(Spore.value(12))
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[String]](json)
      assert("12" == spore2.get())
    }

    test("testSerializeMap") {
      val expected = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$IntToStringSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":12}}"""
      val spore = Spore.value(12).map(IntToStringSpore.build())
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[String]](json)
      assert("12" == spore2.get())
    }

    test("testSerializeFlatMap") {
      val expected = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$StringRW$"},"value":"12"}"""
      val spore = Spore.value(12).flatMap(IntToSporeStringSpore.build())
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[String]](json)
      assert("12" == spore2.get())
    }

    test("testSerializeFooToStringSpore") {
      val expected = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDefs$FooToStringSpore$"}"""
      val spore = FooToStringSpore.build()
      val json = write(spore)
      assert(expected == json)
      val spore2 = read[Spore[Foo => String]](json)
      assert("x: 1, y: a" == spore2.get()(Foo(1, "a")))
    }

    test("testReadPrettyPrintedIntSpore") {
      val json = """{
        |  "tag": "Body",
        |  "kind": 0,
        |  "className": "spores.SporeSerializationTestsDefs$IntSpore$"
        |}""".stripMargin
      val spore = read[Spore[Int]](json)
      assert(12 == spore.get())
    }

    test("testReadPrettyPrintedIntToIntSpore") {
      val json = """{
        |    "tag" : "Body",
        |    "kind" : 0,
        |    "className" : "spores.SporeSerializationTestsDefs$IntToIntSpore$"
        |}""".stripMargin
      val spore = read[Spore[Int => Int]](json)
      assert(24 == spore.get()(12))
    }

    test("testReadSpacedSporeToString") {
      val json1 = """{"tag": "Body", "kind":0, "className": "spores.SporeSerializationTestsDefs$SporeToString$"}"""
      val spore1 = read[Spore[Spore[Int => Boolean] => Int => String]](json1)
      assert("yes" == spore1.get()(IntPredicateSpore.build())(11))
      assert("no" == spore1.get()(IntPredicateSpore.build())(9))
      val json2 = """{
        |"tag": "WithEnv",
        |"fun": {"tag": "Body", "kind": 0, "className": "spores.SporeSerializationTestsDefs$SporeToString$"},
        |"env": {"tag": "Val", "ev": {"tag": "Body", "kind": 0, "className": "spores.ReadWriters$SporeRW$"}, "value": {"tag": "Body", "kind": 0, "className": "spores.SporeSerializationTestsDefs$IntPredicateSpore$"}}
        |}""".stripMargin
      val spore2 = read[Spore[Int => String]](json2)
      assert("yes" == spore2.get()(11))
      assert("no" == spore2.get()(9))
    }

    test("testReadSpacedIntToStringCurried") {
      val json1 = """{"tag":"Body", "kind":0, "className":"spores.SporeSerializationTestsDefs$IntToStringCurried$"}"""
      val spore1 = read[Spore[Int => Int => String]](json1)
      assert("7" == spore1.get()(3)(4))
      val json2 = """{
        |  "tag": "WithEnv",
        |  "fun": {"tag": "Body", "kind": 0, 
        |          "className": "spores.SporeSerializationTestsDefs$IntToStringCurried$"},
        |  "env": {"tag": "Val",
        |    "ev": {"tag": "Body", "kind": 0, "className": "spores.ReadWriters$IntRW$"},
        |    "value": 3}
        |}""".stripMargin
      val spore2 = read[Spore[Int => String]](json2)
      assert("7" == spore2.get()(4))
    }

    test("testReadTabIndentedIntEnv") {
      val json = "{\n\t\"tag\": \"Val\",\n\t\"ev\": {\n\t\t\"tag\": \"Body\",\n\t\t\"kind\": 0,\n\t\t\"className\": \"spores.ReadWriters$IntRW$\"\n\t},\n\t\"value\": 12\n}"
      val spore = read[Spore[Int]](json)
      assert(12 == spore.get())
    }

    test("testReadExtraWhitespaceFooEnv") {
      val json = """{
        |
        |  "tag" :  "Val" ,
        |
        |  "ev" :  {
        |    "tag" :  "Body" ,
        |    "kind" :  0 ,
        |    "className" :  "spores.SporeSerializationTestsDefs$FooRW$"
        |  } ,
        |
        |  "value" :  { "x" : 1 , "y" : "337" }
        |
        |}""".stripMargin
      val spore = read[Spore[Foo]](json)
      assert(Foo(1, "337") == spore.get())
    }

    test("testReadNewlinesOnlySporeIntEnv") {
      val json = """{
        |"tag":"Val",
        |"ev":{
        |"tag":"Body",
        |"kind":0,
        |"className":"spores.ReadWriters$SporeRW$"
        |},
        |"value":{
        |"tag":"Body",
        |"kind":0,
        |"className":"spores.SporeSerializationTestsDefs$IntSpore$"
        |}
        |}""".stripMargin
      val spore = read[Spore[Spore[Int]]](json)
      assert(12 == spore.get().get())
    }

    test("testReadMixedFormattingListIntEnv") {
      val json = """{
        |  "tag": "Val",
        |  "ev": {"tag": "WithEnv", "fun": {"tag": "Body", "kind": 1, "className": "spores.ReadWriters$ListRW"}, "env": {"tag": "Body", "kind": 0, "className": "spores.ReadWriters$IntRW$"}},
        |  "value": [ 1 , 2 , 3 ]
        |}""".stripMargin
      val spore = read[Spore[List[Int]]](json)
      assert(List(1, 2, 3) == spore.get())
    }

    test("testSerializeDeadCodeInt") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntSpore$"}"""
      val spore2 = read[Spore[Int]](json)
      assert(12 == spore2.get())
    }

    test("testSerializeDeadCodeString") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$StringSpore$"}"""
      val spore2 = read[Spore[String]](json)
      assert("Hello" == spore2.get())
    }

    test("testSerializeDeadCodeBoolean") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$BooleanSpore$"}"""
      val spore2 = read[Spore[Boolean]](json)
      assert(true == spore2.get())
    }

    test("testSerializeDeadCodeDouble") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$DoubleSpore$"}"""
      val spore2 = read[Spore[Double]](json)
      assert(0.5 == spore2.get())
    }

    test("testSerializeDeadCodeFloat") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$FloatSpore$"}"""
      val spore2 = read[Spore[Float]](json)
      assert(1.5f == spore2.get())
    }

    test("testSerializeDeadCodeLong") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$LongSpore$"}"""
      val spore2 = read[Spore[Long]](json)
      assert(987654321L == spore2.get())
    }

    test("testSerializeDeadCodeShort") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$ShortSpore$"}"""
      val spore2 = read[Spore[Short]](json)
      assert(12.toShort == spore2.get())
    }

    test("testSerializeDeadCodeByte") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$ByteSpore$"}"""
      val spore2 = read[Spore[Byte]](json)
      assert(7.toByte == spore2.get())
    }

    test("testSerializeDeadCodeChar") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$CharSpore$"}"""
      val spore2 = read[Spore[Char]](json)
      assert('a' == spore2.get())
    }

    test("testSerializeDeadCodeUnit") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$UnitSpore$"}"""
      val spore2 = read[Spore[Unit]](json)
      assert(() == spore2.get())
    }

    test("testSerializeDeadCodeIntToInt") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntToIntSpore$"}"""
      val spore2 = read[Spore[Int => Int]](json)
      assert(24 == spore2.get()(12))
    }

    test("testSerializeDeadCodeIntToString") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntToStringSpore$"}"""
      val spore2 = read[Spore[Int => String]](json)
      assert("12" == spore2.get()(12))
    }

    test("testSerializeDeadCodeStringToInt") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$StringToIntSpore$"}"""
      val spore2 = read[Spore[String => Int]](json)
      assert(5 == spore2.get()("Hello"))
    }

    test("testSerializeDeadCodeIntPredicate") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntPredicateSpore$"}"""
      val spore2 = read[Spore[Int => Boolean]](json)
      assert(true == spore2.get()(11))
      assert(false == spore2.get()(9))
    }

    test("testSerializeDeadCodeIntCtxToString") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntCtxToStringSpore$"}"""
      val spore2 = read[Spore[Int ?=> String]](json)
      assert("12" == spore2.get()(using 12))
    }

    test("testSerializeDeadCodeIntToSporeString") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntToSporeStringSpore$"}"""
      val spore2 = read[Spore[Int => Spore[String]]](json)
      assert("12" == spore2.get()(12).get())
    }

    test("testSerializeDeadCodeIntIntToInt") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntIntToIntSpore$"}"""
      val spore2 = read[Spore[(Int, Int) => Int]](json)
      assert(24 == spore2.get()(5, 7))
    }

    test("testSerializeDeadCodeIntIntToString") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntIntToStringSpore$"}"""
      val spore2 = read[Spore[(Int, Int) => String]](json)
      assert("12" == spore2.get()(5, 7))
    }

    test("testSerializeDeadCodeIntToStringClass") {
      val json = """{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDeadCode$IntToStringClassSpore"}"""
      val spore2 = read[Spore[Int => String]](json)
      assert("12" == spore2.get()(12))
    }

    test("testSerializeDeadCodeIntCtxToStringClass") {
      val json = """{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDeadCode$IntCtxToStringClassSpore"}"""
      val spore2 = read[Spore[Int ?=> String]](json)
      assert("12" == spore2.get()(using 12))
    }

    test("testSerializeDeadCodeTToStringClass") {
      val json = """{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDeadCode$TToStringClassSpore"}"""
      val spore2 = read[Spore[Int => String]](json)
      assert("42" == spore2.get()(42))
    }

    test("testSerializeDeadCodeIdentity") {
      val json = """{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$SporeRW$"},"value":{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDeadCode$Identity"}}"""
      val spore2 = read[Spore[Spore[Int => Int]]](json)
      assert(12 == spore2.get().get()(12))
    }

    test("testSerializeDeadCodeTToTClass") {
      val json = """{"tag":"WithEnv","fun":{"tag":"WithEnv","fun":{"tag":"Body","kind":1,"className":"spores.SporeSerializationTestsDeadCode$TToTClassSpore"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":5}},"env":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntToIntSpore$"}}"""
      val spore2 = read[Spore[Int]](json)
      assert(17 == spore2.get())
    }

    test("testSerializeDeadCodeIntToStringCurried") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntToStringCurried$"}"""
      val spore2 = read[Spore[Int => Int => String]](json)
      assert("7" == spore2.get()(3)(4))
    }

    test("testSerializeDeadCodeSporeToString") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$SporeToString$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$SporeRW$"},"value":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntPredicateSpore$"}}}"""
      val spore2 = read[Spore[Int => String]](json)
      assert("yes" == spore2.get()(11))
      assert("no" == spore2.get()(9))
    }

    test("testSerializeDeadCodeFooToString") {
      val json = """{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$FooToStringSpore$"}"""
      val spore2 = read[Spore[SporeSerializationTestsDeadCode.Foo => String]](json)
      assert("x: 1, y: a" == spore2.get()(SporeSerializationTestsDeadCode.Foo(1, "a")))
    }

    test("testSerializeDeadCodeWithEnv") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntPredicateSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":11}}"""
      val spore2 = read[Spore[Boolean]](json)
      assert(true == spore2.get())
    }

    test("testSerializeDeadCodeWithCtx") {
      val json = """{"tag":"WithEnv","fun":{"tag":"Body","kind":0,"className":"spores.SporeSerializationTestsDeadCode$IntCtxToStringSpore$"},"env":{"tag":"Val","ev":{"tag":"Body","kind":0,"className":"spores.ReadWriters$IntRW$"},"value":7}}"""
      val spore2 = read[Spore[String]](json)
      assert("7" == spore2.get())
    }
  }
}
