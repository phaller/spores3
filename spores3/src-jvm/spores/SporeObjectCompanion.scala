package spores

import upickle.default.ReadWriter

import spores.*
import spores.jvm.*


/** Internal API. Extended from by the [[spores.Spore]] companion object. */
private[spores] trait SporeObjectCompanion {

  // This is a hack for having platform-specific operations in the companion
  // object.


  /** Create a Spore from the provided closure `body` and parameter list of
    * captured variables `captures`.
    *
    * Captured variables in `body` must either be added to the `captures`
    * parameter list, or the "capture all" mode `*` must be used. Mentioned
    * variables in the `captures` list must be used in the `body`. For each
    * capture of type `E` an implicit/given `Spore[ReadWriter[E]]` must be in
    * contextual scope. Deviations will cause a compiler error. The created
    * Spore is safe to serialize and deserialize.
    *
    * @example A Spore with no captures
    *   {{{
    * Spore.apply() { (x: Int) => x + 1 }
    *   }}}
    * @example A Spore with explicit captures
    *   {{{
    * val y = 12
    * val z = 13
    * Spore.apply(y, z) { (x: Int) => x + y + z + 1 }
    *   }}}
    * @example A Spore with `*` capture all mode
    *   {{{
    * val y = 12
    * val z = 13
    * Spore.apply(*) { (x: Int) => x + y + z + 1 }
    *   }}}
    *
    * @param captures
    *   The captured variables used in the `body`, or `*` to capture all by
    *   default.
    * @param body
    *   The closure.
    * @tparam T
    *   The type of the closure.
    * @return
    *   A new `Spore[T]`.
    */
  inline def apply[T](inline captures: Any*)(inline body: T): Spore[T] = {
    SporeJVM0.apply[ReadWriter, T](captures*)(body)
  }

}
