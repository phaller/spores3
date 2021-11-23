package com.phaller.blocks

sealed trait Block[T, R] extends (T => R) {

  type Env

  def apply(x: T): R

  private[blocks] def applyInternal(x: T)(using Block.EnvAsParam[Env]): R
  private[blocks] def envir: Env
}

case class BlockData[E](fqn: String, env: E) {
  def toBlock[T, R]: Block[T, R] { type Env = E } = {
    val creator = Creator[E, T, R](fqn)
    creator(env)
  }
}

object CBlock {

  class CBlockBuilder[E](env: E) {
    def apply[T, R](fqn: String): CBlock[E, T, R] = {
      val creator = Creator[E, T, R](fqn)
      val block = creator(env)
      new CBlock(fqn, block)
    }
  }

  def apply[E](env: E): CBlockBuilder[E] =
    CBlockBuilder(env)

}

class CBlock[E, T, R](
  private[blocks] val creatorName: String,
  private[blocks] val block: Block[T, R] { type Env = E }
) extends Block[T, R] { // must be in same source file as sealed trait Block

  type Env = E

  def apply(x: T): R =
    block(x)

  private[blocks] def applyInternal(x: T)(using Block.EnvAsParam[Env]): R =
    block.applyInternal(x)

  private[blocks] val envir: Env =
    block.envir
}

object Block {

  extension [R](b: Block[Unit, R])
    def apply(): R = b.apply(())

  opaque type EnvAsParam[T] = T

  class Builder[E, T, R](body: T => EnvAsParam[E] ?=> R) {
    def apply(env: E): Block[T, R] { type Env = E } =
      new Block[T, R] {
        type Env = E
        def apply(x: T): R =
          body(x)(using env)
        private[blocks] def applyInternal(x: T)(using EnvAsParam[Env]): R =
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
          private[blocks] def applyInternal(x: A)(using EnvAsParam[Env]): B =
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
          private[blocks] def applyInternal(x: A)(using EnvAsParam[Nothing]): B =
            fun.applyInternal(x)
          private[blocks] def envir =
            throw new Exception("block does not have an environment")
        }
      }
    }

  /** The environment of a block is accessed using `env` from within
    * the body of the block.
    */
  def env[T](using ep: EnvAsParam[T]): T = ep

  /* Requirements:
   * - `body` must be a function literal
   * - `body` must not capture anything
   * - `body` is only allowed to access its parameter and `Block.env`
   */
  def apply[T, A, B](env: T)(body: A => EnvAsParam[T] ?=> B): Block[A, B] { type Env = T } =
    new Block[A, B] {
      type Env = T
      def apply(x: A): B = body(x)(using env)
      private[blocks] def applyInternal(x: A)(using EnvAsParam[T]): B =
        body(x)
      private[blocks] val envir = env
    }

  /* Requirements:
   * - `body` must be a function literal
   * - `body` must not capture anything
   * - `body` is only allowed to access its parameter and `Block.env`
   */
  def apply[T, R](body: T => R): Block[T, R] { type Env = Nothing } =
    new Block[T, R] {
      type Env = Nothing
      def apply(x: T): R = body(x)
      private[blocks] def applyInternal(x: T)(using EnvAsParam[Nothing]): R =
        body(x)
      private[blocks] def envir =
        throw new Exception("block does not have an environment")
    }

  /* Requirements:
   * - `body` must be a function literal
   * - `body` must not capture anything
   * - `body` is only allowed to access its parameter and `Block.env`
   */
  def thunk[T, U](env: T)(body: EnvAsParam[T] ?=> U): Block[Unit, U] { type Env = T } =
    new Block[Unit, U] {
      type Env = T
      def apply(x: Unit): U = body(using env)
      private[blocks] def applyInternal(x: Unit)(using EnvAsParam[T]): U =
        body
      private[blocks] val envir = env
    }

}
