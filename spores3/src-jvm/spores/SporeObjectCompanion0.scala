package spores

import spores.*
import spores.jvm.*


/** Internal API. Extended from by the [[spores.Spore0]] companion object. */
private[spores] trait SporeObjectCompanion0 {
  // This is a hack for having platform-specific operations in the companion
  // object.

  inline def apply[F[_], T](inline body: T): Spore0[F, T] = {
    SporeJVM0.apply(body)
  }

  inline def applyWithEnv[F[_], E, T](inline env: E)(inline body: E => T)(using ev: Spore0[F, F[E]]): Spore0[F, T] = {
    apply[F, E => T](body).withEnv(env)(using ev)
  }

  inline def applyWithCtx[F[_], E, T](inline env: E)(inline body: E ?=> T)(using ev: Spore0[F, F[E]]): Spore0[F, T] = {
    apply[F, E ?=> T](body).withCtx(env)(using ev)
  }

  /** Create a Spore0 from `body`. Automatically captures variables and checks
    * captured variables. Captured variables in `body` must have an implicit
    * `Spore[F[E]]` in scope, where `E` is the type of the captured environment
    * variable and `F[_]` is the type of the captured evidence.
    *
    * If a captured
    * variable does not have an implicit `Spore[F[E]]` in scope then it
    * will cause a compile error.
    *
    * @example
    *   {{{
    * def isBetween(x: Int , y: Int): Spore0[F, Int => Boolean] = {
    *   // `x` and `y` are captured variables of type `Int`, so we need to
    *   // provide an implicit `Spore[F[Int]]` in scope.
    *   Spore0.auto { (i: Int) => x <= i && i < y }
    * }
    *   }}}
    *
    * @param body
    *   The closure.
    * @tparam T
    *   The type of the closure.
    * @tparam F
    *   The type of the captured evidence.
    * @return
    *   A new `Spore[F]` with the packed closure `f`.
    */
  inline def auto[F[_], T](inline body: T): Spore0[F, T] = {
    SporeJVM0.auto(body)
  }
}
