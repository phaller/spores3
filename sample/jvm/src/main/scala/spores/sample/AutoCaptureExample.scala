package spores.sample

import upickle.default.*

import spores.*
import spores.given


object AutoCaptureExample {

  // The `Spore.auto` method does the following:
  //
  // 1. Lifts all captured symbols to parameters
  // 2. Find the implicit readwriters for each captured symbol
  // 3. Pack the new lifted function into a packed spore
  // 4. Pack the captured symbols together with their readwriters
  //
  // Consider the example:
  //
  // {{{
  //   def foo(x: Int, y: Int) = {
  //     Spore.auto{ (i: Int) => x + y + i }
  //   }
  // }}}
  //
  // The closure
  //
  // {{{
  //  { (i: Int) => x + y + i }
  // }}}
  //
  // captures `x` and `y`. This is lifted in the first phase to:
  //
  // {{{
  //   (x: Int) => (y: Int) => (i: Int) => x + y + i)
  // }}}
  //
  // After that, it will pack the lifted function, and pack the captured
  // varibles and their readwriters.


  // A factory for a serialized function that checks if a number is between the
  // numbers `x` and `y`.
  def isBetween(x: Int, y: Int): Spore[Int => Boolean] = {
    Spore.auto { (i: Int) => x <= i && i < y }
  }


  // It is possible to create a custom data type and readwriter, for data that
  // is captured and packed. Here we create a custom `Range` data type, and its
  // corresponding readwriter.
  case class Range(x: Int, y: Int)
  object RangeRW extends SporeBuilder[ReadWriter[Range]]({ macroRW })
  given rangeRW: Spore[ReadWriter[Range]] = RangeRW.build()

  // Now we can create a similar factory but by capturing a `Range` object.
  def isInRange(range: Range): Spore[Int => Boolean] = {
    // The `Spore.auto` method will automatically pack the `Range` object and
    // its readwriter.
    Spore.auto{ (i: Int) => range.x <= i && i < range.y }
  }


  // // If the readwriter is missing, then it is not possible to capture and
  // // pack the value. It will emit the following error:
  // // no implicit values were found that match type spores.Spore[upickle.default.ReadWriter[spores.experimental.example.AutoCaptureExample.Range2]]
  // case class Range2(x: Int, y: Int)
  // def isInRange2(range: Range2): Spore[Int => Boolean] = {
  //   Spore.auto{ (i: Int) => range.x <= i && i < range.y }
  // }


  def main(args: Array[String]): Unit = {
    val btwn1020 = isBetween(10, 20)

    println(btwn1020)
    // result: PackedWithEnv(PackedWithEnv(PackedLambda(spores.experimental.example.AutoCaptureExample$Lambda$1),PackedEnv(10,PackedObject(spores.ReadWriters$IntRW$))),PackedEnv(20,PackedObject(spores.ReadWriters$IntRW$)))

    assert(btwn1020.unwrap().apply(5) == false)
    println(btwn1020.unwrap().apply(5))

    assert(btwn1020.unwrap().apply(15) == true)
    println(btwn1020.unwrap().apply(15))

    assert(btwn1020.unwrap().apply(25) == false)
    println(btwn1020.apply(25))

    val filter = Spore.auto { (l: List[Int]) => l.filter(btwn1020.unwrap()) }

    println(filter)
    // result: PackedWithEnv(PackedLambda(spores.experimental.example.AutoCaptureExample$Lambda$3),PackedEnv({"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedWithEnv","packed":{"$type":"spores.Packed.PackedLambda","className":"spores.experimental.example.AutoCaptureExample$Lambda$1"},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"10","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$IntRW$"}}},"packedEnv":{"$type":"spores.Packed.PackedEnv","env":"20","rw":{"$type":"spores.Packed.PackedObject","className":"spores.ReadWriters$IntRW$"}}},PackedObject(spores.ReadWriters$SporeRW$)))

    val l = List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)
    val expected = List(10, 11, 12, 13, 14, 15, 16, 17, 18, 19)
    assert(filter.unwrap().apply(l) == expected)
    println(filter.unwrap().apply(l))

    val inRange = isInRange(Range(1, 2))

    println(inRange)
    // result: PackedWithEnv(PackedWithEnv(PackedLambda(spores.experimental.example.AutoCaptureExample$Lambda$2),PackedEnv({"x":1,"y":2},PackedObject(spores.experimental.example.AutoCaptureExample$RangeRW$))),PackedEnv(null,PackedObject(spores.ReadWriters$UnitRW$)))

    assert(inRange.unwrap().apply(0) == false)
    println(inRange.unwrap().apply(0))

    assert(inRange.unwrap().apply(1) == true)
    println(inRange.unwrap().apply(1))

    assert(inRange.unwrap().apply(2) == false)
    println(inRange.unwrap().apply(2))
  }
}
