package com.phaller.blocks

sealed trait Block[T, R] extends (T => R) {

  type Env

  def apply(x: T): R

  private[blocks] def applyInternal(x: T)(using Block.EnvAsParam[Env]): R
  private[blocks] def envir: Env
}

sealed trait Thunk[T] extends Block[Unit, T] {
  def apply(): T
  def apply(x: Unit): T = apply()
}

trait DBlock[T, R] extends Block[T, R] {
  self =>

  private[blocks] def envDuplicable: Duplicable[Env]

  def duplicate(): DBlock[T, R] { type Env = self.Env }

  def duplicable: Duplicable[DBlock[T, R] { type Env = self.Env }] =
    new Duplicable[DBlock[T, R] { type Env = self.Env }] {
      def duplicate(value: DBlock[T, R] { type Env = self.Env }) = value.duplicate()
    }
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

  opaque type EnvAsParam[T] = T

  class Creator[E, T, R](body: T => EnvAsParam[E] ?=> R) {
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

  given [E: Duplicable, R]: Duplicable[Thunk[R] { type Env = E }] =
    new Duplicable[Thunk[R] { type Env = E }] {
      def duplicate(fun: Thunk[R] { type Env = E }) = {
        val env = summon[Duplicable[E]].duplicate(fun.envir)
        new Thunk[R] {
          type Env = E
          def apply(): R =
            fun.applyInternal(())(using env)
          private[blocks] def applyInternal(x: Unit)(using EnvAsParam[Env]): R =
            fun.applyInternal(x)
          private[blocks] val envir = env
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
  def thunk[T, U](env: T)(body: EnvAsParam[T] ?=> U): Thunk[U] { type Env = T } =
    new Thunk[U] {
      type Env = T
      def apply(): U = body(using env)
      private[blocks] def applyInternal(x: Unit)(using EnvAsParam[T]): U =
        body
      private[blocks] val envir = env
    }

  def dblock[E: Duplicable, T, R](env: E)(body: T => EnvAsParam[E] ?=> R): DBlock[T, R] =
    new DBlock[T, R] { self =>
      type Env = E
      def apply(x: T): R = body(x)(using env)
      private[blocks] def applyInternal(x: T)(using EnvAsParam[Env]): R =
        body(x)
      private[blocks] val envir = env
      private[blocks] val envDuplicable: Duplicable[Env] = summon[Duplicable[E]]
      def duplicate() = {
        val denv = envDuplicable.duplicate(envir)
        new DBlock[T, R] {
          type Env = E
          def apply(x: T): R = body(x)(using denv)
          private[blocks] def applyInternal(x: T)(using EnvAsParam[Env]): R =
            body(x)
          private[blocks] val envir = denv
          private[blocks] val envDuplicable: Duplicable[Env] = self.envDuplicable
          def duplicate() = self.duplicate()
        }
      }
    }

  def dblock[T, R](body: T => R): DBlock[T, R] =
    new DBlock[T, R] { self =>
      type Env = Nothing
      def apply(x: T): R = body(x)
      private[blocks] def applyInternal(x: T)(using EnvAsParam[Env]): R =
        body(x)
      private[blocks] def envir =
        throw new Exception("block does not have an environment")
      private[blocks] def envDuplicable: Duplicable[Env] =
        throw new Exception("block does not have an environment")
      def duplicate() = {
        new DBlock[T, R] {
          type Env = Nothing
          def apply(x: T): R = body(x)
          private[blocks] def applyInternal(x: T)(using EnvAsParam[Env]): R =
            body(x)
          private[blocks] def envir =
            throw new Exception("block does not have an environment")
          private[blocks] def envDuplicable: Duplicable[Env] =
            throw new Exception("block does not have an environment")
          def duplicate() = self.duplicate()
        }
      }
    }

}
