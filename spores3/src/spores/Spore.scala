package spores

import scala.annotation.implicitNotFound
import upickle.default.*


/** A serializable closure of type `T`. Guaranteed to not cause runtime errors
  * when created, serialized, deserialized, and unwrapped.
  *
  * Use [[unwrap]] to extract the packed closure.
  *
  * Use [[withEnv]] to partially apply the closure of type `T1 => R` to a value
  * of type `T1`. Use [[withEnv2]] to apply it to a value of type `Spore[T1]`.
  *
  * Use [[withCtx]] to partially apply the closure of type `T1 ?=> R` to a value
  * of type `T1`. Use [[withCtx2]] to apply it to a value of type `Spore[T1]`.
  *
  * Spores are created by:
  *   - (JVM) The [[Spore]] lambda factories: `apply`. Requires explicit capture
  *     of environment variables.
  *   - (JVM) The [[Spore]] lambda factory: `auto`. Implicitly captures
  *     environment variables.
  *   - (JVM, Native, ScalaJS) Packing a top-level object which extends the
  *     [[SporeBuilder]] trait.
  *   - (JVM, Native, ScalaJS) Packing a top-level class which extends the
  *     [[SporeClassBuilder]] trait.
  *   - (JVM, Native, ScalaJS) The [[Env]] factory for packing a value of type
  *     `T` for which there is a Spore[ReadWriter[T]].
  *
  * Serializing and deserializing a Spore is easiest done by using the `upickle`
  * library.
  *
  * Compile-time macros guarantee that it is safe to create, serialize,
  * deserialize, and unwrap the packed closure. Creating a Spore is guaranteed
  * to not cause runtime errors. Serializing, deserializing, and unwrapping a
  * Spore is guaranteed to not cause runtime errors.
  *
  * @example
  *   {{{
  * val mySpore: Spore[Int => String] = Spork.apply { x => x.toString.reverse }
  * val myAppliedSpore: Spore[String] = mySpore.withEnv(10)
  * val serialized = upickle.default.write(myAppliedSpore)
  * val deserialized = upickle.default.read[Spore[String]](serialized)
  * val unwrapped = deserialized.unwrap()
  * unwrapped // "01"
  *   }}}
  *
  * @tparam T
  *   The type of the packed closure.
  */
sealed trait Spore[+T] {

  /** Applies the packed closure to a value of type `T1`. Only available if the
    * wrapped closure of type `T` is a subtype of `T1 => R`.
    *
    * The value is packed together with the packed closure. When unwrapping,
    * both are individually unwrapped and applied to each other.
    *
    * @param env
    *   The value applied to the packed closure.
    * @param ev
    *   The implicit `Spore[ReadWriter[T1]]` used for packing the `env`.
    * @tparam T1
    *   The type of the value applied to the packed closure.
    * @tparam R
    *   The return type of the packed closure.
    * @return
    *   A new `Spore[R]` with the result of the application.
    */
  def withEnv[T1, R](env: T1)(using ev: Spore[ReadWriter[T1]])(using @implicitNotFound(CanWithEnv.MSG) ev2: CanWithEnv[T, T1, R]): Spore[R] = {
    AST.WithEnv(this, AST.Val(ev, env))
  }

  /** Optimization for applying this `Spore[T1 => R]` directly to a `Spore[T1]`.
    *
    * This avoids the need to pack the `env` as it already is a Spore.
    *
    * Only available if the wrapped closure of type `T` is a subtype of `T1 =>
    * R`.
    */
  def withEnv2[T1, R](env: Spore[T1])(using @implicitNotFound(CanWithEnv.MSG) ev2: CanWithEnv[T, T1, R]): Spore[R] = {
    AST.WithEnv(this, env)
  }

  /** Applies the packed closure to a context value of type `T1`. Only available
    * if the wrapped closure of type `T` is a subtype of `T1 ?=> R`.
    *
    * The context value is packed together with the packed closure. When
    * unwrapping, both are individually unwrapped and applied to each other.
    *
    * @param env
    *   The context value applied to the packed closure.
    * @param ev
    *   The implicit `Spore[ReadWriter[T1]]` used for packing the `env`.
    * @tparam T1
    *   The type of the context value applied to the packed closure.
    * @tparam R
    *   The return type of the packed closure.
    * @return
    *   A new `Spore[R]` with the result of the application.
    */
  def withCtx[T1, R](env: T1)(using ev: Spore[ReadWriter[T1]])(using @implicitNotFound(CanWithCtx.MSG) ev2: CanWithCtx[T, T1, R]): Spore[R] = {
    AST.WithCtx(this, AST.Val(ev, env))
  }

  /** Optimization for applying this `Spore[T1 ?=> R]` directly to a
    * `Spore[T1]`.
    *
    * This avoids the need to pack the `env` as it already is a Spore.
    *
    * Only available if the wrapped closure of type `T` is a subtype of `T1 ?=>
    * R`.
    */
  def withCtx2[T1, R](env: Spore[T1])(using @implicitNotFound(CanWithCtx.MSG) ev2: CanWithCtx[T, T1, R]): Spore[R] = {
    AST.WithCtx(this, env)
  }

  def map[U](fun: Spore[T => U]): Spore[U] = {
    fun.withEnv2(this)
  }

  def flatMap[U](fun: Spore[T => Spore[U]]): Spore[U] = {
    fun.withEnv2(this).unwrap()
  }

  /** Unwraps and returns the packed closure.
    *
    * @return
    *   The unwrapped closure of type `T`.
    */
  def unwrap(): T = {
    this match
      case AST.Body(_, _, body) => body
      case AST.Val(_, value) => value
      case AST.WithEnv(fun, env) => fun.unwrap()(env.unwrap())
      case AST.WithCtx(fun, env) => fun.unwrap()(using env.unwrap())
  }

}


/** A factory for creating Spores that are safe to serialize and deserialize.
  *
  * Note: The Spore factory methods only work on the JVM. Use the
  * [[spores.SporeBuilder]] or [[spores.SporeClassBuilder]] if ScalaJS or
  * ScalaNative support is needed.
  */
object Spore extends SporeObjectCompanionJVM


private object AST {
  case class Body[+T](kind: Int, className: String, body: T) extends Spore[T]
  case class Val[T](ev: Spore[ReadWriter[T]], value: T) extends Spore[T]
  case class WithEnv[E, +R](fun: Spore[E => R], env: Spore[E]) extends Spore[R]
  case class WithCtx[E, +R](fun: Spore[E ?=> R], env: Spore[E]) extends Spore[R]
}


private type CanWithEnv[T, T1, R] = Spore[T] <:< Spore[T1 => R]
private object CanWithEnv { inline val MSG = "Cannot pack contained type ${T} with environment type ${T1}. It is not a function type of ${T1} => ${R}." }

private type CanWithCtx[T, T1, R] = Spore[T] <:< Spore[T1 ?=> R]
private object CanWithCtx { inline val MSG = "Cannot pack contained type ${T} with context type ${T1}. It is not a function type of ${T1} ?=> ${R}." }
