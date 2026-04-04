package spores

import upickle.default.*

import spores.Reflection
import spores.AST


/** Internal API. Used by the spores.jvm.Spore lambda factories. */
@Reflection.EnableReflectiveInstantiation
private[spores] trait SporeLambdaBuilder[+T](val body: T) {

  final inline def build(): Spore[T] = {
    ${ SporeLambdaBuilder.buildMacro('this) }
  }
}


private object SporeLambdaBuilder {
  import scala.quoted.*

  def buildMacro[T](expr: Expr[SporeLambdaBuilder[T]])(using Type[T], Quotes): Expr[Spore[T]] = {
    // No checks needed, all relevant checks are done in the spores.jvm.Spore lambda factories.
    '{ AST.Body(2, $expr.getClass().getName(), $expr.body) }
  }
}
