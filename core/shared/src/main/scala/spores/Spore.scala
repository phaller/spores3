package spores

import _root_.upickle.default.*

import scala.quoted.*


/**
  * The type of a *spore*, a special kind of closure with an explicit
  * environment. The environment of a spore is a single, internal
  * value or reference whose type is indicated by the `Spore` trait's
  * `Env` type member.
  *
  * Spores are created in one of two ways: either using the factory
  * methods of the `Spore` companion object, or using a *spore
  * builder*. Spore builders are top-level objects extending either
  * [[Builder]] or [[Spore.Builder]].
  *
  * Like a regular function type, the type of spores is contravariant
  * in its parameter type and covariant in its result type.
  *
  * @tparam T the parameter type
  * @tparam R the result type
  */
sealed trait Spore[-T, +R] extends (T => R) {

  /** The type of the spore's environment.
    */
  type Env

  /** Applies the spore to the given argument.
    *
    * @param x the argument of the spore application
    */
  def apply(x: T): R

  private[spores] def applyInternal(x: T)(env: Env): R =
    throw new Exception("Method must be overridden")

  private[spores] def envir: Env

}

private def checkBodyExpr[T, S](bodyExpr: Expr[T => S])(using Quotes): Unit = {
  import quotes.reflect.*

  def symIsToplevelObject(sym: Symbol): Boolean =
    sym.flags.is(Flags.Module) && sym.owner.flags.is(Flags.Package)

  def ownerChainContains(sym: Symbol, transitiveOwner: Symbol): Boolean =
    if (sym.maybeOwner.isNoSymbol) false
    else ((sym.owner == transitiveOwner) || ownerChainContains(sym.owner, transitiveOwner))

  def checkCaptures(defdefSym: Symbol, anonfunBody: Tree): Unit = {
    /* collect all identifier uses.
       check that they don't have an owner outside the anon fun.
       uses of top-level objects are OK.
     */

    val acc = new TreeAccumulator[List[Ident]] {
      def foldTree(ids: List[Ident], tree: Tree)(owner: Symbol): List[Ident] = tree match {
        case id @ Ident(_) => id :: ids
        case _ =>
          try {
            foldOverTree(ids, tree)(owner)
          } catch {
            case me: MatchError =>
              // compiler bug: skip checking tree
              ids
          }
      }
    }
    val foundIds = acc.foldTree(List(), anonfunBody)(defdefSym)
    val foundSyms = foundIds.map(id => id.symbol)
    val names = foundSyms.map(sym => sym.name)
    val ownerNames = foundSyms.map(sym => sym.owner.name)

    val allOwnersOK = foundSyms.forall(sym =>
      ownerChainContains(sym, defdefSym) ||
        symIsToplevelObject(sym) || ((!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner)) || ((!sym.maybeOwner.isNoSymbol) && (!sym.owner.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner.owner))) // example: `ExecutionContext.Implicits.global`

    // report error if not all owners OK
    if (!allOwnersOK) {
      foundIds.foreach { id =>
        val sym = id.symbol
        val isOwnedByToplevelObject =
          symIsToplevelObject(sym) || ((!sym.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner)) || ((!sym.maybeOwner.isNoSymbol) && (!sym.owner.maybeOwner.isNoSymbol) && symIsToplevelObject(sym.owner.owner))

        val isOwnedBySpore = ownerChainContains(sym, defdefSym)
        if (!isOwnedByToplevelObject) {
          // might find illegal capturing
          if (!isOwnedBySpore)
            report.error(s"Invalid capture of variable `${id.name}`. Use first parameter of spore's body to refer to the spore's environment.", id.pos)
        }
      }
    }
  }

  val tree = bodyExpr.asTerm
  tree match {
    case Inlined(None, List(),
      TypeApply(Select(Block(List(), Block(
        List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)
      )), asInst), _)
    ) =>
      checkCaptures(defdef.symbol, anonfunBody)

    case Inlined(None, List(),
      TypeApply(Select(Block(
        List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)
      ), asInst), _)
    ) =>
      checkCaptures(defdef.symbol, anonfunBody)

    case Inlined(None, List(),
      Block(List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _))) =>
      checkCaptures(defdef.symbol, anonfunBody)

    case Inlined(None, List(), Block(List(),
      Block(List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)))) =>
      checkCaptures(defdef.symbol, anonfunBody)

    case _ =>
      val str = tree.show(using Printer.TreeStructure)
      report.error(s"Argument must be a function literal", tree.pos)
  }
}

/** Used for creating serializable spores that don't have an
  * environment. Example:
  *
  * ```scala
  * object SporeWithoutEnv extends Builder[Int, Int](
  *   (x: Int) => x + 1
  * )
  * ```
  *
  * Builders can be used to create [[SporeData]] instances, which are
  * serializable.
  *
  * @tparam T the parameter type
  * @tparam R the result type
  */
class Builder[T, R](fun: T => R) extends TypedBuilder[Nothing, T, R] {

  private[spores] def createSpore(envOpt: Option[String]): Spore[T, R] =
    apply() // envOpt is empty

  def apply[E](): Spore[T, R] { type Env = E } =
    new Spore[T, R] {
      type Env = E
      def apply(x: T): R =
        fun(x)
      override private[spores] def applyInternal(x: T)(env: Env): R =
        fun(x)
      private[spores] def envir =
        throw new Exception("spore does not have an environment")
    }

}

trait TypedBuilder[E, T, R] extends PackedBuilder[T, R]

trait PackedBuilder[T, R] {
  private[spores] def createSpore(envOpt: Option[String]): Spore[T, R]
}

/** The `Spore` companion object provides factory methods as well as
  * the [[Spore.Builder]] class for creating spore builders.
  */
object Spore {

  /** Applies a spore with parameter type `Unit`.
    */
  extension [R](spore: Spore[Unit, R])
    def apply(): R = spore.apply(())

  /** Used for creating serializable spores. The first step is to create
    * a top-level object that extends `Spore.Builder`:
    *
    * ```scala
    * object MySpore extends Spore.Builder[Int, Int, Int](
    *   env => (x: Int) => env + x + 1
    * )
    * ```
    *
    * The builder's first type argument specifies the environment
    * type.  The body of the spore refers to the spore's environment
    * using the extra `env` parameter.  By providing a concrete
    * environment, an actual spore can be created as follows:
    *
    * ```scala
    * val x = 12
    * val sp = MySpore(x)  // environment is integer value 12
    * ```
    *
    * Builders can be used to create [[SporeData]] instances, which
    * are serializable.
    *
    * @tparam E the environment type
    * @tparam T the parameter type
    * @tparam R the result type
    */
  class Builder[E, T, R](fun: E => T => R)(using ReadWriter[E]) extends TypedBuilder[E, T, R] {

    private[spores] def createSpore(envOpt: Option[String]): Spore[T, R] = {
      // actually creates a Spore[T, R] { type Env = E }
      // envOpt is non-empty
      val env = read[E](envOpt.get)
      apply(env)
    }

    def apply(env: E): Spore[T, R] { type Env = E } =
      new Spore[T, R] {
        type Env = E
        def apply(x: T): R =
          fun(env)(x)
        override private[spores] def applyInternal(x: T)(y: Env): R =
          fun(y)(x)
        private[spores] val envir =
          env
      }

  }

  given [E: Duplicable, T, R]: Duplicable[Spore[T, R] { type Env = E }] =
    new Duplicable[Spore[T, R] { type Env = E }] {
      def duplicate(spore: Spore[T, R] { type Env = E }) = {
        val duplicatedEnv = summon[Duplicable[E]].duplicate(spore.envir)
        new Spore[T, R] {
          type Env = E
          def apply(x: T): R =
            spore.applyInternal(x)(duplicatedEnv)
          override private[spores] def applyInternal(x: T)(y: Env): R =
            spore.applyInternal(x)(y)
          private[spores] val envir = duplicatedEnv
        }
      }
    }

  // how to duplicate a spore without environment
  given [T, R]: Duplicable[Spore[T, R] { type Env = Nothing }] =
    new Duplicable[Spore[T, R] { type Env = Nothing }] {
      def duplicate(spore: Spore[T, R] { type Env = Nothing }) = {
        new Spore[T, R] {
          type Env = Nothing
          def apply(x: T): R =
            spore.apply(x) // ignore environment
          override private[spores] def applyInternal(x: T)(y: Nothing): R =
            spore.applyInternal(x)(y)
          private[spores] def envir =
            throw new Exception("spore does not have an environment")
        }
      }
    }

  /** Creates a spore given an environment value/reference and a
    * function.  The given function must not capture anything.  The
    * second (curried) parameter must be used to access the spore's
    * environment. The given function must be a *function literal*
    * which is checked at compile time (using a macro).
    *
    * @tparam E the type of the spore's environment
    * @tparam T the spore's parameter type
    * @tparam R the spore's result type
    * @param env  the spore's environment
    * @param body the spore's body
    * @return a spore initialized with the given environment and body
    */
  inline def apply[E, T, R](inline initEnv: E)(inline body: E => T => R): Spore[T, R] { type Env = E } =
    ${ applyCode('initEnv, 'body) }

  private def applyCode[E, T, R](envExpr: Expr[E], bodyExpr: Expr[E => T => R])(using Type[E], Type[T], Type[R], Quotes): Expr[Spore[T, R] { type Env = E }] = {
    checkBodyExpr(bodyExpr)

    '{
      new Spore[T, R] {
        type Env = E
        def apply(x: T): R = $bodyExpr($envExpr)(x)
        override private[spores] def applyInternal(x: T)(env: E): R =
          $bodyExpr(env)(x)
        private[spores] val envir = $envExpr
      }
    }
  }

  /** Creates a spore without an environment. The given body (function)
    * must not capture anything.
    *
    * @tparam T the spore's parameter type
    * @tparam R the spore's result type
    * @param body the spore's body
    * @return a spore with the given body
    */
  inline def apply[T, R](inline body: T => R): Spore[T, R] { type Env = Nothing } =
    ${ applyCode('body) }

  private def applyCode[T, R](bodyExpr: Expr[T => R])(using Type[T], Type[R], Quotes): Expr[Spore[T, R] { type Env = Nothing }] = {
    checkBodyExpr(bodyExpr)

    '{
      new Spore[T, R] {
        type Env = Nothing
        def apply(x: T): R = $bodyExpr(x)
        private[spores] def envir =
          throw new Exception("spore does not have an environment")
      }
    }
  }

  /** Creates a thunk spore. The given body must be a function literal
    * that does not capture anything.
    *
    * @tparam T the type of the spore's environment
    * @tparam R the spore's result type
    * @param env  the spore's environment
    * @param body the spore's body
    * @return a spore initialized with the given environment and body
    */
  inline def thunk[E, R](inline env: E)(inline body: E => R): Spore[Unit, R] { type Env = E } =
    ${ thunkCode('env, 'body) }

  private def thunkCode[E, R](envExpr: Expr[E], bodyExpr: Expr[E => R])(using Type[E], Type[R], Quotes): Expr[Spore[Unit, R] { type Env = E }] = {
    checkBodyExpr(bodyExpr)

    '{
      new Spore[Unit, R] {
        type Env = E
        def apply(x: Unit): R = $bodyExpr($envExpr)
        override private[spores] def applyInternal(x: Unit)(env: E): R =
          $bodyExpr(env)
        private[spores] val envir = $envExpr
      }
    }
  }

}
