package spores

import upickle.default.*

import spores.Reflection


/** A builder trait that packs a [[Spore]] with a closure of type `T`. Extend
  * this trait from a **top-level object** and provide the closure as a trait
  * parameter.
  *
  * Note: Use [[SporeClassBuilder]] if type parameters are needed.
  *
  * Note: Must be extended from a top-level object. An object is considered
  * top-level if it is nested directly inside a package, or nested inside
  * another top-level object. Extending from a class will result in a
  * compile-time error when calling [[build]].
  *
  * @example
  *   {{{
  * object MyBuilder extends SporeBuilder[Int => String](x => x.toString().reverse)
  * val mySpore: Spore[Int => String] = MyBuilder.build()
  *   }}}
  *
  * @tparam T
  *   The type of the wrapped closure.
  * @param body
  *   The wrapped closure.
  */
@Reflection.EnableReflectiveInstantiation
trait SporeBuilder[+T] extends SporeBuilder0[ReadWriter, T]
