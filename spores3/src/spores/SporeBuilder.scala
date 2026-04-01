package spores

import upickle.default.*

import spores.Reflection
import spores.Packed.*


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
  * @param fun
  *   The wrapped closure.
  */
@Reflection.EnableReflectiveInstantiation
trait SporeBuilder[+T](private[spores] val fun: T) {

  /** Packs the wrapped closure into a [[Spore]] of type `T`.
    *
    * @return
    *   A new Spore with the wrapped closure.
    */
  final inline def build(): Spore[T] = {
    ${ SporeBuilder.buildMacro('this) }
  }
}


private object SporeBuilder {
  import scala.quoted.*

  def buildMacro[T](expr: Expr[SporeBuilder[T]])(using Type[T], Quotes): Expr[Spore[T]] = {
    Macros.isTopLevelObject(expr)
    '{ PackedObject($expr.getClass().getName()) }
  }
}
