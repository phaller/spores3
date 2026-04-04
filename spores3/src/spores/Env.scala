package spores

import upickle.default.*

import spores.*


/** A factory for packing environment values of type `T` into `Spore[T]` by
  * using an implicit `Spore[ReadWriter[T]]` instance.
  */
object Env {

  /** Packs a value of type `T` as a `Spore[T]`.
    *
    * @param env
    *   The value to pack.
    * @param ev
    *   The implicit `Spore[ReadWriter[T]]` used for packing the `env`.
    * @tparam T
    *   The type of the value to pack.
    * @return
    *   A new `Spore[T]` with the packed `env`.
    */
  def apply[T](env: T)(using ev: Spore[ReadWriter[T]]): Spore[T] = {
    AST.Val(ev, env)
  }
}
