package spores

import spores.*
import spores.jvm.*


/** Internal API. Extended from by the [[spores.Spore0]] companion object. */
private[spores] trait SporeObjectCompanion0 {

  // This is a hack for having platform-specific operations in the companion
  // object.


  /** Create a Spore0 from the provided closure `body` and parameter list of
    * captured variables `captures` with evidence type `F[_]`.
    *
    * Captured variables in `body` must either be added to the `captures`
    * parameter list, or the "capture all" mode `*` must be used. Mentioned
    * variables in the `captures` list must be used in the `body`. For each
    * capture of type `E` an implicit/given `Spore0[F, F[E]]` must be in
    * contextual scope. Deviations will cause a compiler error.
    *
    * @example A Spore with no captures
    *   {{{
    * Spore0.apply[F, Int => Int]() { (x: Int) => x + 1 }
    *   }}}
    * @example A Spore with explicit captures
    *   {{{
    * val y = 12
    * val z = 13
    * Spore0.apply[F, Int => Int](y, z) { (x: Int) => x + y + z + 1 }
    *   }}}
    * @example A Spore with `*` capture all mode
    *   {{{
    * val y = 12
    * val z = 13
    * Spore0.apply[F, Int => Int](*) { (x: Int) => x + y + z + 1 }
    *   }}}
    *
    * @param captures
    *   The captured variables used in the `body`, or `*` to capture all by
    *   default.
    * @param body
    *   The closure.
    * @tparam F
    *  The evidence type for the captures. Each capture of type `E` requires an
    *  implicit/given `Spore0[F, F[E]]` in contextual scope.
    * @tparam T
    *   The type of the closure.
    * @return
    *   A new `Spore0[F, T]`.
    */
  inline def apply[F[_], T](inline captures: Any*)(inline body: T): Spore0[F, T] = {
    SporeJVM0.apply[F, T](captures*)(body)
  }

}
