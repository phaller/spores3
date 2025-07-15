package spores

import scala.annotation.targetName
import upickle.default.ReadWriter

import spores.*


/** Internal API. Extended from by the [[spores.Spore]] companion object. This
  * is a hack for having platform-specific operations in the companion object.
  */
private[spores] trait SporeObjectCompanionJVM {

  /** Create a Spore from the provided closure `fun`.
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
    * @param fun
    *   The closure.
    * @tparam T
    *   The type of the closure.
    * @return
    *   A new `Spore[T]` with the packed closure `fun`.
    */
  inline def apply[T](inline fun: T): Spore[T] = {
    spores.jvm.Spore.apply(fun)
  }

  /** Create a Spore from the provided closure `fun` with an environment
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
    * @param fun
    *   The closure.
    * @param rw
    *   The implicit `Spore[ReadWriter[E]]` used for packing the `env`.
    * @tparam E
    *   The type of the environment variable.
    * @tparam T
    *   The return type of the closure.
    * @return
    *   A new `Spore[T]` with the packed closure `fun` applied to the `env`.
    */
  inline def applyWithEnv[E, T](inline env: E)(inline fun: E => T)(using rw: Spore[ReadWriter[E]]): Spore[T] = {
    spores.jvm.Spore.applyWithEnv(env)(fun)
  }

  /** Create a Spore from the provided closure `fun` with an environment
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
    * @param fun
    *   The closure.
    * @param rw
    *   The implicit `Spore[ReadWriter[E]]` used for packing the `env`.
    * @tparam E
    *   The type of the context environment variable.
    * @tparam T
    *   The return type of the closure.
    * @return
    *   A new `Spore[T]` with the packed closure `fun` using the implicit `env`.
    */
  inline def applyWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using rw: Spore[ReadWriter[E]]): Spore[T] = {
    spores.jvm.Spore.applyWithCtx(env)(fun)
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
    * @param f
    *   The closure.
    * @tparam F
    *   The type of the closure.
    * @return
    *   A new `Spore[F]` with the packed closure `f`.
    */
  inline def auto[T](inline fun: T): Spore[T] = {
    spores.jvm.AutoCapture.apply(fun)
  }
}
