package com.phaller.blocks

import scala.annotation.targetName

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
sealed trait Block[-T, +R] extends (T => R) {

  /** The type of the block's environment.
    */
  type Env

  /** Applies the block to the given argument.
    *
    * @param x the argument of the block application
    */
  def apply(x: T): R

  private[blocks] def applyInternal(x: T)(using Block.EnvAsParam[Env]): R =
    throw new Exception("Method must be overridden")

  private[blocks] def envir: Env

}

class Builder[T, R](body: T => R) extends TypedBuilder[Nothing, T, R] {

  private[blocks] def createBlock(envOpt: Option[String]): Block[T, R] =
    apply() // envOpt is empty

  def apply[E](): Block[T, R] { type Env = E } =
    new Block[T, R] {
      type Env = E
      def apply(x: T): R =
        body(x)
      override private[blocks] def applyInternal(x: T)(using Block.EnvAsParam[Env]): R =
        body(x)
      private[blocks] def envir =
        throw new Exception("block does not have an environment")
    }

}

trait TypedBuilder[E, T, R] extends PackedBuilder[T, R]

trait PackedBuilder[T, R] {
  private[blocks] def createBlock(envOpt: Option[String]): Block[T, R]
}

/** The `Block` companion object provides factory methods and the
  * [[Block.Builder]] class for creating blocks, as well as the `env`
  * member used to access the environment of a block from within the
  * block's body.
  */
object Block {

  /** Applies a block with parameter type `Unit`.
    */
  extension [R](block: Block[Unit, R])
    def apply(): R = block.apply(())

  /** The type under which the environment of a block is accessible.
    *
    * Only used internally (hence opaque).
    */
  opaque type EnvAsParam[T] = T

  class Builder[E, T, R](body: T => EnvAsParam[E] ?=> R)(using ReadWriter[E]) extends TypedBuilder[E, T, R] {

    private[blocks] def createBlock(envOpt: Option[String]): Block[T, R] = {
      // actually creates a Block[T, R] { type Env = E }
      // envOpt is non-empty
      val env = read[E](envOpt.get)
      apply(env)
    }

    def apply(env: E): Block[T, R] { type Env = E } =
      new Block[T, R] {
        type Env = E
        def apply(x: T): R =
          body(x)(using env)
        override private[blocks] def applyInternal(x: T)(using EnvAsParam[Env]): R =
          body(x)
        private[blocks] val envir =
          env
      }

  }

  given [E: Duplicable, A, B]: Duplicable[Block[A, B] { type Env = E }] =
    new Duplicable[Block[A, B] { type Env = E }] {
      def duplicate(fun: Block[A, B] { type Env = E }) = {
        val env = summon[Duplicable[E]].duplicate(fun.envir)
        new Block[A, B] {
          type Env = E
          def apply(x: A): B =
            fun.applyInternal(x)(using env)
          override private[blocks] def applyInternal(x: A)(using EnvAsParam[Env]): B =
            fun.applyInternal(x)
          private[blocks] val envir = env
        }
      }
    }

  // how to duplicate a block without environment
  given [A, B]: Duplicable[Block[A, B] { type Env = Nothing }] =
    new Duplicable[Block[A, B] { type Env = Nothing }] {
      def duplicate(fun: Block[A, B] { type Env = Nothing }) = {
        new Block[A, B] {
          type Env = Nothing
          def apply(x: A): B =
            fun.apply(x) // ignore environment
          override private[blocks] def applyInternal(x: A)(using EnvAsParam[Nothing]): B =
            fun.applyInternal(x)
          private[blocks] def envir =
            throw new Exception("block does not have an environment")
        }
      }
    }

  /** The environment of a block is accessed using `env` from within
    * the body of the block.
    *
    * @tparam E the type of the environment of the current block
    * @return the environment of the current block
    */
  def env[E](using ep: EnvAsParam[E]): E = ep

  /** Creates a block given an environment value/reference and a
    * function.  The given function must not capture anything; the
    * `env` member must be used to access the block's environment.  In
    * order to create a block with environment, the given function
    * must be a *function literal*.
    *
    * @tparam E the type of the block's environment
    * @tparam T the block's parameter type
    * @tparam R the block's result type
    * @param env  the block's environment
    * @param body the block's body
    * @return a block initialized with the given environment and body
    */
  inline def apply[E, T, R](inline env: E)(inline body: T => EnvAsParam[E] ?=> R): Block[T, R] { type Env = E } =
    ${ applyCode('env)('body) }

  def checkBodyExpr[T, S](bodyExpr: Expr[T => S])(using Quotes): Unit = {
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
          case _ => foldOverTree(ids, tree)(owner)
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
              report.error(s"Invalid capture of variable `${id.name}`. Use `Block.env` to refer to the block's environment.", id.pos)
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
        BlockTree(List(defdef @ DefDef(anonfun, params, _, Some(anonfunBody))), Closure(_, _))) =>
        checkCaptures(defdef.symbol, anonfunBody)

      case _ =>
        val str = tree.show(using Printer.TreeStructure)
        report.error(s"Argument must be a function literal, but found: $str", tree.pos)
    }
  }

  private def applyCode[E, T, R](envExpr: Expr[E])(bodyExpr: Expr[T => EnvAsParam[E] ?=> R])(using Type[E], Type[T], Type[R], Quotes): Expr[Block[T, R] { type Env = E }] = {
    checkBodyExpr(bodyExpr)

    '{
      new Block[T, R] {
        type Env = E
        def apply(x: T): R = $bodyExpr(x)(using $envExpr)
        override private[blocks] def applyInternal(x: T)(using EnvAsParam[E]): R =
          $bodyExpr(x)
        private[blocks] val envir = $envExpr
      }
    }
  }

  /*@targetName("and")
  def &[E, T, R](env: E)(body: T => EnvAsParam[E] ?=> R): Block[T, R] { type Env = E } =
    apply[E, T, R](env)(body)*/

  /** Creates a block without an environment. The given body (function)
    * must not capture anything.
    *
    * @tparam T the block's parameter type
    * @tparam R the block's result type
    * @param body the block's body
    * @return a block with the given body
    */
  inline def apply[T, R](inline body: T => R): Block[T, R] { type Env = Nothing } =
    ${ applyCode('body) }

  private def applyCode[T, R](bodyExpr: Expr[T => R])(using Type[T], Type[R], Quotes): Expr[Block[T, R] { type Env = Nothing }] = {
    checkBodyExpr(bodyExpr)

    '{
      new Block[T, R] {
        type Env = Nothing
        def apply(x: T): R = $bodyExpr(x)
        private[blocks] def envir =
          throw new Exception("block does not have an environment")
      }
    }
  }

  /*@targetName("and")
  def &[T, R](body: T => R): Block[T, R] { type Env = Nothing } =
    apply[T, R](body)*/

  /* Requirements:
   * - `body` must be a function literal
   * - `body` must not capture anything
   */
  def thunk[T, U](env: T)(body: EnvAsParam[T] ?=> U): Block[Unit, U] { type Env = T } =
    new Block[Unit, U] {
      type Env = T
      def apply(x: Unit): U = body(using env)
      override private[blocks] def applyInternal(x: Unit)(using EnvAsParam[T]): U =
        body
      private[blocks] val envir = env
    }

}
