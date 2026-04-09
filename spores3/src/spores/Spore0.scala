package spores

import spores.*


sealed trait Spore0[F[_], +T] { self: Spore0[F, T] =>
  import Spore0.AST

  
  /** Returns the value of the packed closure.
    *
    * @return
    *   The value of the packed closure of type `T`.
    */
  final def get(): T = {
    this match {
      case AST.Body(_, _, body)   => body
      case AST.Value(_, value)    => value
      case AST.WithEnv(body, env) => body.get().apply(env.get())
    }
  }

  private final def safeUpcast[T2](using ev: T <:< T2): Spore0[F, T2] = {
    ev.substituteCo(this)
  }

  private final def safeCast0[E1, R](using ev: T <:< (E1 ?=> R)): Spore0[F, E1 => R] = {
    this.asInstanceOf[Spore0[F, E1 => R]] // Safe as context function becomes normal function after erasure
  }

  /** Applies the packed closure to a value of type `E1`. Only available if the
    * wrapped closure of type `T` is a subtype of `E1 => R`.
    *
    * The value is packed together with the packed closure. When unwrapping,
    * both are individually unwrapped and applied to each other.
    *
    * @param env
    *   The value the packed closure is applied to.
    * @param ev1
    *   The implicit `Spore0[F, F[E1]]` used for packing the `env`.
    * @tparam E1
    *   The type of the value the packed closure is applied to.
    * @tparam R
    *   The return type of the packed closure.
    * @return
    *   A new `Spore0[F, R]` with the result of the application.
    */  
  final def withEnv[E1, R](
      env: E1,
  )(using
      ev1: Spore0[F, F[E1]],
  )(using
      ev2: T <:< (E1 => R),
  ): Spore0[F, R] = {
    AST.WithEnv(
      this.safeUpcast[E1 => R],
      AST.Value(
        ev1,
        env,
      ),
    )
  }

  /** Optimization for applying this `Spore0[F, E1 => R]` directly to a
    * `Spore0[F, E1]`.
    *
    * This avoids the need to pack the `env` as it already is a Spore.
    *
    * Only available if the wrapped closure of type `T` is a subtype of `E1 =>
    * R`.
    */
  final def withEnv2[E1, R](
      env: Spore0[F, E1],
  )(using
      ev2: T <:< (E1 => R),
  ): Spore0[F, R] = {
    AST.WithEnv(
      this.safeUpcast[E1 => R],
      env,
    )
  }

  /** Applies the packed closure to a context value of type `E1`. Only available
    * if the wrapped closure of type `T` is a subtype of `E1 ?=> R`.
    *
    * The context value is packed together with the packed closure. When
    * unwrapping, both are individually unwrapped and applied to each other.
    *
    * @param env
    *   The context value the packed closure is applied to.
    * @param ev1
    *   The implicit `Spore0[F, F[E1]]` used for packing the `env`.
    * @tparam E1
    *   The type of the context value the packed closure is applied to.
    * @tparam R
    *   The return type of the packed closure.
    * @return
    *   A new `Spore0[F, R]` with the result of the application.
    */
  final def withCtx[E1, R](
      env: E1,
  )(using
      ev1: Spore0[F, F[E1]],
  )(using
      ev2: T <:< (E1 ?=> R),
  ): Spore0[F, R] = {
    AST.WithEnv(
      this.safeCast0.safeUpcast[E1 => R],
      AST.Value(
        ev1,
        env,
      ),
    )
  }

  /** Optimization for applying this `Spore0[F, E1 ?=> R]` directly to a
    * `Spore0[F, E1]`.
    *
    * This avoids the need to pack the `env` as it already is a Spore.
    *
    * Only available if the wrapped closure of type `T` is a subtype of `E1 ?=>
    * R`.
    */
  final def withCtx2[E1, R](
      env: Spore0[F, E1],
  )(using
      ev2: T <:< (E1 ?=> R),
  ): Spore0[F, R] = {
    AST.WithEnv(
      this.safeCast0.safeUpcast[E1 => R],
      env,
    )
  }

  final def map[U](sp: Spore0[F, T => U]): Spore0[F, U] = {
    sp.withEnv2(this)
  }

  final def flatMap[U](sp: Spore0[F, T => Spore0[F, U]]): Spore0[F, U] = {
    sp.withEnv2(this).get()
  }
}


/** A factory for creating Spores that are safe to serialize and deserialize.
  *
  * Note: The Spore factory methods only work on the JVM. Use the
  * [[spores.SporeBuilder0]] or [[spores.SporeClassBuilder0]] for ScalaJS and
  * ScalaNative.
  */
object Spore0 extends SporeObjectCompanion0 {

  /** Pack a value of type `T` as a `Spore0[F, T]` using an implicit
    * `Spore0[F, F[T]]` instance.
    *
    * @param v
    *   The value to pack.
    * @param ev
    *   The implicit `Spore0[F, F[T]]` used for packing the value.
    * @tparam T
    *   The type of the value to pack.
    * @return
    *   A new `Spore0[F, T]` with the packed value.
    */
  def value[F[_], T](v: T)(using ev: Spore0[F, F[T]]): Spore0[F, T] = {
    AST.Value(ev, v)
  }


  private[spores] object AST {
    final case class Body[F[_], +T](
        className: String,
        kind: Int, // kind: 0 = SporeBuilder0; 1 = SporeClassBuilder0; 2 = SporeLambdaBuilder0
        body: T,
    ) extends Spore0[F, T]

    final case class Value[F[_], T](
        ev: Spore0[F, F[T]],
        value: T,
    ) extends Spore0[F, T]

    final case class WithEnv[F[_], E, +R](
        fun: Spore0[F, E => R],
        env: Spore0[F, E],
    ) extends Spore0[F, R]
  }
}
