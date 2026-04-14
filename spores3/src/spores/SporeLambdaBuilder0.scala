package spores

import spores.Reflection


@Reflection.EnableReflectiveInstantiation
private[spores] trait SporeLambdaBuilder0[F[_], +T] {
  private[spores] def body: T

  private[spores] final inline def build(): Spore0[F, T] = {
    ${ SporeLambdaBuilder0.buildMacro('{this}) }
  }
}


private object SporeLambdaBuilder0 {
  import scala.quoted.*

  def buildMacro[F[_], T](expr: Expr[SporeLambdaBuilder0[F, T]])(using Type[F], Type[T], Quotes): Expr[Spore0[F, T]] = {
    // No checks needed, all relevant checks are done in the spores.jvm.Spore lambda factories.
    '{ Spore0.AST.Body($expr.getClass().getName(), 2, $expr.body) }
  }
}
