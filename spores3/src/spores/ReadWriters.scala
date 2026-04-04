package spores

import upickle.default.*

import spores.*


/** A collection of ReadWriters. Contains both `ReadWriter[Spore[T]]` and
  * `Spore[ReadWriter[T]]` for various `T`.
  *
  * Use `ReadWriter[Spore[T]]` to serialize and deserialize Spores. For example,
  * by using the `upickle.default.write` and `upickle.default.read` methods
  * applied to a `Spore[T]`.
  *
  * Use `Spore[ReadWriter[T]]` when packing a value of type `T` into a Spore.
  * For example, by using the `withEnv` and `withCtx` methods of a `Spore[T =>
  * R]` or `Spore[T ?=> R]`.
  */
object ReadWriters {

  //////////////////////////////////////////////////////////////////////////////
  // ReadWriter[Spore[T]]
  //////////////////////////////////////////////////////////////////////////////

  given [T]: ReadWriter[Spore[T]] =
    summon[ReadWriter[ujson.Value]].bimap[Spore[T]](
      sp => {
        sp match {
          case AST.Body(kind, className, _) =>
            ujson.Obj(
              "tag" -> ujson.Str("Body"),
              "kind" -> ujson.Num(kind),
              "className" -> ujson.Str(className),
            )
          case AST.Val(ev, value) =>
            ujson.Obj(
              "tag" -> ujson.Str("Val"),
              "ev" -> writeJs(ev),
              "value" -> writeJs(value)(using ev.unwrap()),
            )
          case AST.WithEnv(fun, env) =>
            ujson.Obj(
              "tag" -> ujson.Str("WithEnv"),
              "fun" -> writeJs(fun),
              "env" -> writeJs(env),
            )
          case AST.WithCtx(fun, env) =>
            ujson.Obj(
              "tag" -> ujson.Str("WithCtx"),
              "fun" -> writeJs(fun),
              "env" -> writeJs(env),
            )
        }
      },
      js => {
        js("tag").str match {
          case "Body" =>
            val kind = js("kind").num.toInt
            val className = js("className").str
            val body = kind match {
              case 0 =>
                Reflection.loadModuleFieldValue[SporeBuilder[T]](className).body
              case 1 =>
                Reflection.loadClassInstance[SporeClassBuilder[T]](className).body
              case 2 =>
                Reflection.loadClassInstance[SporeLambdaBuilder[T]](className).body
            }
            AST.Body(kind, className, body)
          case "Val" =>
            val ev = read[Spore[ReadWriter[T]]](js("ev"))
            val value = read[T](js("value"))(using ev.unwrap())
            AST.Val(ev, value)
          case "WithEnv" =>
            val fun = read[Spore[Any => T]](js("fun"))
            val env = read[Spore[Any]](js("env"))
            AST.WithEnv(fun, env)
          case "WithCtx" =>
            val fun = read[Spore[Any ?=> T]](js("fun"))
            val env = read[Spore[Any]](js("env"))
            AST.WithCtx(fun, env)
        }
      },
    )

  //////////////////////////////////////////////////////////////////////////////
  // Spore[ReadWriter[T]] for primitive T
  //////////////////////////////////////////////////////////////////////////////

  private[spores] object IntRW extends SporeBuilder[ReadWriter[Int]](summon[ReadWriter[Int]])
  given intRW: Spore[ReadWriter[Int]] = IntRW.build()

  private[spores] object StringRW extends SporeBuilder[ReadWriter[String]](summon[ReadWriter[String]])
  given strRW: Spore[ReadWriter[String]] = StringRW.build()

  private[spores] object BooleanRW extends SporeBuilder[ReadWriter[Boolean]](summon[ReadWriter[Boolean]])
  given boolRW: Spore[ReadWriter[Boolean]] = BooleanRW.build()

  private[spores] object DoubleRW extends SporeBuilder[ReadWriter[Double]](summon[ReadWriter[Double]])
  given doubleRW: Spore[ReadWriter[Double]] = DoubleRW.build()

  private[spores] object FloatRW extends SporeBuilder[ReadWriter[Float]](summon[ReadWriter[Float]])
  given floatRW: Spore[ReadWriter[Float]] = FloatRW.build()

  private[spores] object LongRW extends SporeBuilder[ReadWriter[Long]](summon[ReadWriter[Long]])
  given longRW: Spore[ReadWriter[Long]] = LongRW.build()

  private[spores] object ShortRW extends SporeBuilder[ReadWriter[Short]](summon[ReadWriter[Short]])
  given shortRW: Spore[ReadWriter[Short]] = ShortRW.build()

  private[spores] object ByteRW extends SporeBuilder[ReadWriter[Byte]](summon[ReadWriter[Byte]])
  given byteRW: Spore[ReadWriter[Byte]] = ByteRW.build()

  private[spores] object CharRW extends SporeBuilder[ReadWriter[Char]](summon[ReadWriter[Char]])
  given charRW: Spore[ReadWriter[Char]] = CharRW.build()

  private[spores] object UnitRW extends SporeBuilder[ReadWriter[Unit]](summon[ReadWriter[Unit]])
  given unitRW: Spore[ReadWriter[Unit]] = UnitRW.build()

  //////////////////////////////////////////////////////////////////////////////
  // Spore[ReadWriter[Spore[?]]]
  //////////////////////////////////////////////////////////////////////////////

  private[spores] object SporeRW extends SporeBuilder[ReadWriter[Spore[?]]](summon[ReadWriter[Spore[?]]])
  given packedSporeRW[T]: Spore[ReadWriter[Spore[T]]] = SporeRW.build().asInstanceOf[Spore[ReadWriter[Spore[T]]]]

  //////////////////////////////////////////////////////////////////////////////
  // Spore[ReadWriter[F[T]]] for Option[T], List[T], etc.
  //////////////////////////////////////////////////////////////////////////////

  private[spores] class SomeRW[T] extends SporeClassBuilder[ReadWriter[T] ?=> ReadWriter[Some[T]]]({ summon })
  given someRW[T](using tRW: Spore[ReadWriter[T]]): Spore[ReadWriter[Some[T]]] = new SomeRW[T].build().withCtx2(tRW)

  private[spores] object NoneRW extends SporeBuilder[ReadWriter[None.type]](summon[ReadWriter[None.type]])
  given noneRW: Spore[ReadWriter[None.type]] = NoneRW.build()

  private[spores] class OptionRW[T] extends SporeClassBuilder[ReadWriter[T] ?=> ReadWriter[Option[T]]]({ summon })
  given optionRW[T](using tRW: Spore[ReadWriter[T]]): Spore[ReadWriter[Option[T]]] = new OptionRW[T].build().withCtx2(tRW)

  private[spores] class ListRW[T] extends SporeClassBuilder[ReadWriter[T] ?=> ReadWriter[List[T]]]({ summon })
  given listRW[T](using tRW: Spore[ReadWriter[T]]): Spore[ReadWriter[List[T]]] = new ListRW[T].build().withCtx2(tRW)

  private[spores] class Tuple1RW[T1] extends SporeClassBuilder[ReadWriter[T1] ?=> ReadWriter[Tuple1[T1]]]({ summon })
  given tuple1RW[T1](using t1RW: Spore[ReadWriter[T1]]): Spore[ReadWriter[Tuple1[T1]]] = new Tuple1RW[T1].build().withCtx2(t1RW)

  private[spores] class Tuple2RW[T1, T2] extends SporeClassBuilder[ReadWriter[T1] ?=> ReadWriter[T2] ?=> ReadWriter[Tuple2[T1, T2]]]({ summon })
  given tuple2RW[T1, T2](using t1RW: Spore[ReadWriter[T1]], t2RW: Spore[ReadWriter[T2]]): Spore[ReadWriter[Tuple2[T1, T2]]] = (new Tuple2RW[T1, T2]).build().withCtx2(t1RW).withCtx2(t2RW)

  private[spores] class Tuple3RW[T1, T2, T3] extends SporeClassBuilder[ReadWriter[T1] ?=> ReadWriter[T2] ?=> ReadWriter[T3] ?=> ReadWriter[Tuple3[T1, T2, T3]]]({ summon })
  given tuple3RW[T1, T2, T3](using t1RW: Spore[ReadWriter[T1]], t2RW: Spore[ReadWriter[T2]], t3RW: Spore[ReadWriter[T3]]): Spore[ReadWriter[Tuple3[T1, T2, T3]]] = (new Tuple3RW[T1, T2, T3]).build().withCtx2(t1RW).withCtx2(t2RW).withCtx2(t3RW)

  private[spores] class EitherRW[Err, T] extends SporeClassBuilder[ReadWriter[Err] ?=> ReadWriter[T] ?=> ReadWriter[Either[Err, T]]]({ summon })
  given eitherRW[Err, T](using ev1: Spore[ReadWriter[Err]], ev2: Spore[ReadWriter[T]]): Spore[ReadWriter[Either[Err, T]]] = new EitherRW[Err, T]().build().withCtx2(ev1).withCtx2(ev2)

}
