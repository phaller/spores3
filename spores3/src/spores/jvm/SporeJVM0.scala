package spores.jvm

import spores.*
import scala.quoted.*


/** Internal API. */
private[spores] object SporeJVM0 {

  // The Spore factory only works on the JVM. The generated class here is not a
  // top-level class. For this reason, it cannot be reflectively instantiated on
  // ScalaJS or ScalaNative. For more information, see:
  // https://github.com/portable-scala/portable-scala-reflect.

  inline def apply[F[_], T](inline captures: Any*)(inline body: T): Spore0[F, T] = {
    ${ applyMacro[F, T]('captures, 'body) }
  }

  private def applyMacro[F[_], T](capturesExpr: Expr[Seq[Any]], bodyExpr: Expr[T])(using Type[F], Type[T], Quotes): Expr[Spore0[F, T]] = {
    Macros.spore0ApplyMacro[F, T](capturesExpr, bodyExpr)
  }
}
