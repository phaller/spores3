package spores


sealed trait Duplicate[T] {
  def unwrap(): T
}

private case class DuplicateLambda[T](fun: T) extends Duplicate[T] {
  def unwrap(): T = fun
}

private case class DuplicateEnv[T](env: T, duplicable: Duplicable[T]) extends Duplicate[T] {
  def unwrap(): T = env
}

private case class DuplicateWithEnv[E, T](fun: Duplicate[E => T], env: Duplicate[E]) extends Duplicate[T] {
  def unwrap(): T = fun.unwrap().apply(env.unwrap())
}

private case class DuplicateWithCtx[E, T](fun: Duplicate[E ?=> T], env: Duplicate[E]) extends Duplicate[T] {
  def unwrap(): T = fun.unwrap().apply(using env.unwrap())
}

object Duplicate {

  import scala.quoted.*

  inline def apply[T](inline fun: T): Duplicate[T] = {
    ${ applyMacro('fun) }
  }

  inline def env[T](inline env: T)(using Duplicable[T]): Duplicate[T] = {
    DuplicateEnv(Duplicable.duplicate(env), summon[Duplicable[T]])
  }

  inline def applyWithEnv[E, T](inline env: E)(inline fun: E => T)(using Duplicable[E]): Duplicate[T] = {
    DuplicateWithEnv(apply(fun), Duplicate.env(env))
  }

  inline def applyWithCtx[E, T](inline env: E)(inline fun: E ?=> T)(using Duplicable[E]): Duplicate[T] = {
    DuplicateWithCtx(apply(fun), Duplicate.env(env))
  }

  private def applyMacro[T](bodyExpr: Expr[T])(using Type[T], Quotes): Expr[Duplicate[T]] = {
    Macros.checkBodyExpr(bodyExpr)
    '{
      DuplicateLambda(${ bodyExpr })
    }
  }

  given [T]: Duplicable[Duplicate[T]] = new Duplicable[Duplicate[T]] {
    def duplicate(value: Duplicate[T]): Duplicate[T] = {
      value match {
        case DuplicateLambda(fun) =>
          DuplicateLambda(fun)
        case DuplicateEnv(env, duplicable) =>
          DuplicateEnv(Duplicable.duplicate(env)(using duplicable), duplicable)
        case DuplicateWithEnv(fun, env) =>
          DuplicateWithEnv(Duplicable.duplicate(fun), Duplicable.duplicate(env))
        case DuplicateWithCtx(fun, env) =>
          DuplicateWithCtx(Duplicable.duplicate(fun), Duplicable.duplicate(env))
      }
    }
  }
}
