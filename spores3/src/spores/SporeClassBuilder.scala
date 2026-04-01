package spores

import upickle.default.*

import spores.Reflection
import spores.Packed.*


/** A class-based builder trait that packs a [[Spore]] with a closure of type
  * `T`. Extend this trait from a **top-level class** and provide the closure as
  * a trait parameter.
  *
  * Note: To be used instead of [[SporeBuilder]] when type parameters are
  * needed.
  *
  * Note: Must be extended from a top-level class. A class is considered
  * top-level if it is nested directly inside a package, or nested inside a
  * top-level object. Extending from an object will result in a compile-time
  * error when calling [[build]].
  *
  * @example
  *   {{{
  * class MyBuilder[T] extends SporeClassBuilder[T => String](x => x.toString().reverse)
  * val mySpore: Spore[Int => String] = new MyBuilder[Int]().build()
  *   }}}
  *
  * @tparam T
  *   The type of the wrapped closure.
  * @param fun
  *   The wrapped closure.
  */
@Reflection.EnableReflectiveInstantiation
trait SporeClassBuilder[+T](private[spores] val fun: T) {

  /** Packs the wrapped closure into a [[Spore]] of type `T`.
    *
    * @return
    *   A new Spore with the wrapped closure.
    */
  final inline def build(): Spore[T] = {
    ${ SporeClassBuilder.buildMacro('this) }
  }
}


private object SporeClassBuilder {
  import scala.quoted.*

  def buildMacro[T](expr: Expr[SporeClassBuilder[T]])(using Type[T], Quotes): Expr[Spore[T]] = {
    Macros.isTopLevelClass(expr)
    '{ PackedClass($expr.getClass().getName()) }
  }
}
