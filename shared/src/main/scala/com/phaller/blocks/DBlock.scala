package com.phaller.blocks


object DBlock {

  def apply[T, R, B <: Block[T, R]](block: B)(using Duplicable[block.Env]): DBlock[T, R] =
    new DBlockWithEnv[T, R](block)

  def apply[T, R, B <: Block[T, R] { type Env = Nothing }](block: B): DBlock[T, R] =
    new DBlockWithoutEnv[T, R](block)

}

trait DBlock[T, R] { self =>
  type Env
  def block: Block[T, R] { type Env = self.Env }
  def duplicable: Duplicable[Block[T, R] { type Env = self.Env }]
  def duplicate(): Block[T, R] { type Env = self.Env } =
    duplicable.duplicate(block)
}

private class DBlockWithEnv[T, R](val b: Block[T, R])(using Duplicable[b.Env]) extends DBlock[T, R] {
  type Env = b.Env
  val block = b
  val duplicable = summon[Duplicable[Block[T, R] { type Env = b.Env }]]
}

private class DBlockWithoutEnv[T, R](val block: Block[T, R] { type Env = Nothing }) extends DBlock[T, R] {
  type Env = Nothing
  val duplicable = summon[Duplicable[Block[T, R] { type Env = Nothing }]]
}
