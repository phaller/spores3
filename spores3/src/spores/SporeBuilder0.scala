package spores

import spores.Reflection


@Reflection.EnableReflectiveInstantiation
trait SporeBuilder0[F[_], +T] {
  private[spores] def body: T

  /** Packs the wrapped closure into a [[Spore]] of type `T`.
    *
    * @return
    *   A new Spore with the wrapped closure.
    */
  final inline def build(): Spore0[F, T] = {
    ${ SporeBuilder0.buildMacro('{this}) }
  }
}


private object SporeBuilder0 {
  import scala.quoted.*

  def buildMacro[F[_], T](expr: Expr[SporeBuilder0[F, T]])(using Type[F], Type[T], Quotes): Expr[Spore0[F, T]] = {
    spores.Macros.isTopLevelObject(expr)
    '{ Spore0.AST.Body($expr.getClass().getName(), 0, $expr.body) }
  }
}
