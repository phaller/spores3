package com.phaller.blocks

import upickle.default.*

import scala.quoted.*


/**
  * The type of a *block*, a special kind of closure with an explicit
  * environment. The environment of a block is a single, internal
  * value or reference whose type is indicated by the `Block` trait's
  * `Env` type member.
  *
  * Blocks are created in one of two ways: either using the factory
  * methods of the `Block` companion object, or using a *block
  * builder*. Block builders are top-level objects extending either
  * [[Builder]] or [[Block.Builder]].
  *
  * Like a regular function type, the type of blocks is contravariant
  * in its parameter type and covariant in its result type.
  *
  * @tparam T the parameter type
  * @tparam R the result type
  */
sealed trait Spore[-T, +R] extends (T => R) {

  /** The type of the block's environment.
    */
  type Env

  /** Applies the block to the given argument.
    *
    * @param x the argument of the block application
    */
  def apply(x: T): R

  private[blocks] def applyInternal(x: T)(env: Env): R =
    throw new Exception("Method must be overridden")

  private[blocks] def envir: Env

}

private def checkBodyExpr[T, S](bodyExpr: Expr[T => S])(using Quotes): Unit = {
  import quotes.reflect.{Block => BlockTree, *}

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

        val isOwnedByBlock = ownerChainContains(sym, defdefSym)
        if (!isOwnedByToplevelObject) {
          // might find illegal capturing
          if (!isOwnedByBlock)
            report.error(s"Invalid capture of variable `${id.name}`. Use first parameter of block's body to refer to the block's environment.", id.pos)
        }
      }
    }
  }

  val tree = bodyExpr.asTerm
  tree match {
    case Inlined(None, List(),
      TypeApply(Select(BlockTree(List(), BlockTree(
        List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)
      )), asInst), _)
    ) =>
      checkCaptures(defdef.symbol, anonfunBody)

    case Inlined(None, List(),
      TypeApply(Select(BlockTree(
        List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)
      ), asInst), _)
    ) =>
      checkCaptures(defdef.symbol, anonfunBody)

    case Inlined(None, List(),
      BlockTree(List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _))) =>
      checkCaptures(defdef.symbol, anonfunBody)

    case Inlined(None, List(), BlockTree(List(),
      BlockTree(List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _)))) =>
      checkCaptures(defdef.symbol, anonfunBody)

    case _ =>
      val str = tree.show(using Printer.TreeStructure)
      report.error(s"Argument must be a function literal", tree.pos)
  }
}

class Builder[T, R](fun: T => R) extends TypedBuilder[Nothing, T, R] {

  private[blocks] def createSpore(envOpt: Option[String]): Spore[T, R] =
    apply() // envOpt is empty

  def apply[E](): Spore[T, R] { type Env = E } =
    new Spore[T, R] {
      type Env = E
      def apply(x: T): R =
        fun(x)
      override private[blocks] def applyInternal(x: T)(env: Env): R =
        fun(x)
      private[blocks] def envir =
        throw new Exception("block does not have an environment")
    }

}

trait TypedBuilder[E, T, R] extends PackedBuilder[T, R]

trait PackedBuilder[T, R] {
  private[blocks] def createSpore(envOpt: Option[String]): Spore[T, R]
}

/** The `Block` companion object provides factory methods as well as
  * the [[Block.Builder]] class for creating block builders.
  */
object Spore {

  /** Applies a block with parameter type `Unit`.
    */
  extension [R](spore: Spore[Unit, R])
    def apply(): R = spore.apply(())

  class Builder[E, T, R](fun: E => T => R)(using ReadWriter[E]) extends TypedBuilder[E, T, R] {

    private[blocks] def createSpore(envOpt: Option[String]): Spore[T, R] = {
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
        override private[blocks] def applyInternal(x: T)(y: Env): R =
          fun(y)(x)
        private[blocks] val envir =
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
          override private[blocks] def applyInternal(x: T)(y: Env): R =
            spore.applyInternal(x)(y)
          private[blocks] val envir = duplicatedEnv
        }
      }
    }

  // how to duplicate a block without environment
  given [T, R]: Duplicable[Spore[T, R] { type Env = Nothing }] =
    new Duplicable[Spore[T, R] { type Env = Nothing }] {
      def duplicate(spore: Spore[T, R] { type Env = Nothing }) = {
        new Spore[T, R] {
          type Env = Nothing
          def apply(x: T): R =
            spore.apply(x) // ignore environment
          override private[blocks] def applyInternal(x: T)(y: Nothing): R =
            spore.applyInternal(x)(y)
          private[blocks] def envir =
            throw new Exception("block does not have an environment")
        }
      }
    }

  /** Creates a block given an environment value/reference and a
    * function.  The given function must not capture anything.  The
    * second (curried) parameter must be used to access the block's
    * environment. The given function must be a *function literal*
    * which is checked at compile time (using a macro).
    *
    * @tparam E the type of the block's environment
    * @tparam T the block's parameter type
    * @tparam R the block's result type
    * @param env  the block's environment
    * @param body the block's body
    * @return a block initialized with the given environment and body
    */
  inline def apply[E, T, R](inline initEnv: E)(inline body: E => T => R): Spore[T, R] { type Env = E } =
    ${ applyCode('initEnv, 'body) }

  private def applyCode[E, T, R](envExpr: Expr[E], bodyExpr: Expr[E => T => R])(using Type[E], Type[T], Type[R], Quotes): Expr[Spore[T, R] { type Env = E }] = {
    checkBodyExpr(bodyExpr)

    '{
      new Spore[T, R] {
        type Env = E
        def apply(x: T): R = $bodyExpr($envExpr)(x)
        override private[blocks] def applyInternal(x: T)(env: E): R =
          $bodyExpr(env)(x)
        private[blocks] val envir = $envExpr
      }
    }
  }

  /** Creates a block without an environment. The given body (function)
    * must not capture anything.
    *
    * @tparam T the block's parameter type
    * @tparam R the block's result type
    * @param body the block's body
    * @return a block with the given body
    */
  inline def apply[T, R](inline body: T => R): Spore[T, R] { type Env = Nothing } =
    ${ applyCode('body) }

  private def applyCode[T, R](bodyExpr: Expr[T => R])(using Type[T], Type[R], Quotes): Expr[Spore[T, R] { type Env = Nothing }] = {
    checkBodyExpr(bodyExpr)

    '{
      new Spore[T, R] {
        type Env = Nothing
        def apply(x: T): R = $bodyExpr(x)
        private[blocks] def envir =
          throw new Exception("block does not have an environment")
      }
    }
  }

  /** Creates a thunk block. The given body must be a function literal
    * that does not capture anything.
    *
    * @tparam T the type of the block's environment
    * @tparam R the block's result type
    * @param env  the block's environment
    * @param body the block's body
    * @return a block initialized with the given environment and body
    */
  inline def thunk[E, R](inline env: E)(inline body: E => R): Spore[Unit, R] { type Env = E } =
    ${ thunkCode('env, 'body) }

  private def thunkCode[E, R](envExpr: Expr[E], bodyExpr: Expr[E => R])(using Type[E], Type[R], Quotes): Expr[Spore[Unit, R] { type Env = E }] = {
    checkBodyExpr(bodyExpr)

    '{
      new Spore[Unit, R] {
        type Env = E
        def apply(x: Unit): R = $bodyExpr($envExpr)
        override private[blocks] def applyInternal(x: Unit)(env: E): R =
          $bodyExpr(env)
        private[blocks] val envir = $envExpr
      }
    }
  }

}
