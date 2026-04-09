package spores

import scala.annotation.targetName
import upickle.default.ReadWriter

import spores.*
import spores.jvm.*


/** Internal API. Extended from by the [[spores.Spore]] companion object. */
private[spores] trait SporeObjectCompanion {

  // This is a hack for having platform-specific operations in the companion
  // object.

  /** Create a Spore from the provided closure `body`.
    *
    * The created Spore is safe to serialize and deserialize. The closure must
    * not capture any variables, otherwise it will cause a compile error.
    *
    * To capture variables, use [[applyWithEnv]], [[applyWithCtx]], or
    * [[auto]] instead.
    *
    * @example
    *   {{{
    * val mySpore = Spore.apply[Int => String] { x => x.toString.reverse }
    *   }}}
    *
    * @param body
    *   The closure.
    * @tparam T
    *   The type of the closure.
    * @return
    *   A new `Spore[T]` with the packed closure `body`.
    */
  inline def apply[T](inline body: T): Spore[T] = {
    SporeJVM.apply(body)
  }

  /** Create a Spore from the provided closure `body` with an environment
    * variable `env` as the first parameter of the closure.
    *
    * The created Spore is safe to serialize and deserialize. The closure must
    * not capture any variables, otherwise it will cause a compile error.
    *
    * @example
    *   {{{
    * val mySpore = Spore.applyWithEnv[Int, String](11) { env => (env + 12).toString.reverse }
    *   }}}
    *
    * @param env
    *   The environment variable applied to the closure.
    * @param body
    *   The closure.
    * @param ev
    *   The implicit `Spore[ReadWriter[E]]` used for packing the `env`.
    * @tparam E
    *   The type of the environment variable.
    * @tparam T
    *   The return type of the closure.
    * @return
    *   A new `Spore[T]` with the packed closure `body` applied to the `env`.
    */
  inline def applyWithEnv[E, T](inline env: E)(inline body: E => T)(using ev: Spore[ReadWriter[E]]): Spore[T] = {
    SporeJVM.applyWithEnv(env)(body)
  }

  /** Create a Spore from the provided closure `body` with an environment
    * variable `env` as the first **implicit** parameter of the closure.
    *
    * The created Spore is safe to serialize and deserialize. The closure must
    * not capture any variables, otherwise it will cause a compile error.
    *
    * @example
    *   {{{
    * val mySpore = Spore.applyWithCtx[Int, String](11) { env ?=> (env + 12).toString.reverse }
    *   }}}
    *
    * @param env
    *   The context environment variable applied to the closure.
    * @param body
    *   The closure.
    * @param ev
    *   The implicit `Spore[ReadWriter[E]]` used for packing the `env`.
    * @tparam E
    *   The type of the context environment variable.
    * @tparam T
    *   The return type of the closure.
    * @return
    *   A new `Spore[T]` with the packed closure `body` using the implicit `env`.
    */
  inline def applyWithCtx[E, T](inline env: E)(inline body: E ?=> T)(using ev: Spore[ReadWriter[E]]): Spore[T] = {
    SporeJVM.applyWithCtx(env)(body)
  }

  /** Create a Spore from `f`. Automatically captures variables and checks
    * captured variables. Captured variables in `f` must have an implicit
    * `Spore[ReadWriter[T]]` in scope, where `T` is the type of the captured
    * variable.
    *
    * The created Spore is safe to serialize and deserialize. If a captured
    * variable does not have an implicit `Spore[ReadWriter[T]]` in scope then it
    * will cause a compile error.
    *
    * @example
    *   {{{
    * def isBetween(x: Int , y: Int): Spore[Int => Boolean] = {
    *   // `x` and `y` are captured variables of type `Int`, so we need to
    *   // provide an implicit `Spore[ReadWriter[Int]]` in scope.
    *   Spore.auto { (i: Int) => x <= i && i < y }
    * }
    *   }}}
    *
    * @param body
    *   The closure.
    * @tparam T
    *   The type of the closure.
    * @return
    *   A new `Spore[F]` with the packed closure `f`.
    */
  inline def auto[T](inline body: T): Spore[T] = {
    SporeJVM.auto(body)
  }
}
