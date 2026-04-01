package spores.jvm

import upickle.default.*

import spores.*
import spores.Packed.*


/** Internal API. Used by the [[spores.SporeObjectCompanionJVM]] factories.
  * Contains factory methods for creating `Spore`s with explicit environments.
  *
  * Note: If a variable is captured then the code will not compile. Use the
  * [[spores.jvm.AutoCapture]] factory if you want to implicitly capture
  * variables. See the docs for a full discussion on when variables are
  * captured.
  */
private[spores] object Spore {

  // The Spore factory only works on the JVM. The generated class here is not a
  // top-level class. For this reason, it cannot be reflectively instantiated on
  // ScalaJS or ScalaNative. For more information, see:
  // https://github.com/portable-scala/portable-scala-reflect.

  inline def apply[T](inline fun: T): Spore[T] = {
    ${ applyMacro('fun) }
  }

  inline def applyWithEnv[E, T](inline env: E)(inline fun: E => T)(using rw: Spore[ReadWriter[E]]): Spore[T] = {
    apply[E => T](fun).withEnv(env)(using rw)
  }

  inline def applyWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using rw: Spore[ReadWriter[E]]): Spore[T] = {
    apply[E ?=>T](fun).withCtx(env)(using rw)
  }

  import scala.quoted.*

  private def applyMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[Spore[T]] = {
    Macros.checkBodyExpr(bodyExpr)
    '{
      class Lambda extends SporeLambdaBuilder[T]($bodyExpr)
      (new Lambda()).build()
    }
  }
}
