package spores

import spores.Reflection


@Reflection.EnableReflectiveInstantiation
trait SporeClassBuilder0[F[_], +T] {
  private[spores] def body: T

  /** Packs the wrapped closure into a [[Spore]] of type `T`.
    *
    * @return
    *   A new Spore with the wrapped closure.
    */
  final inline def build(): Spore0[F, T] = {
    ${ SporeClassBuilder0.buildMacro('{this}) }
  }
}


private object SporeClassBuilder0 {
  import scala.quoted.*

  def buildMacro[F[_], T](expr: Expr[SporeClassBuilder0[F, T]])(using Type[F], Type[T], Quotes): Expr[Spore0[F, T]] = {
    spores.Macros.isTopLevelClass(expr)
    '{ Spore0.AST.Body($expr.getClass().getName(), 1, $expr.body) }
  }
}
