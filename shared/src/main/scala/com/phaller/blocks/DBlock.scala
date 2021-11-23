package com.phaller.blocks


object DBlock {

  def apply[T, R, B <: Block[T, R]](block: B)(using Duplicable[block.Env]): DBlock[T, R] =
    new DBlockWithEnv[T, R](block)

  def apply[T, R, B <: Block[T, R] { type Env = Nothing }](block: B): DBlock[T, R] =
    new DBlockWithoutEnv[T, R](block)

}

trait DBlock[T, R] {
  type TheEnv
  def block: Block[T, R] { type Env = TheEnv }
  def duplicable: Duplicable[Block[T, R] { type Env = TheEnv }]
  def duplicate(): Block[T, R] { type Env = TheEnv } =
    duplicable.duplicate(block)
}

class DBlockWithEnv[T, R](val b: Block[T, R])(using Duplicable[b.Env]) extends DBlock[T, R] {
  type TheEnv = b.Env
  val block = b
  val duplicable = summon[Duplicable[Block[T, R] { type Env = TheEnv }]]
}

class DBlockWithoutEnv[T, R](val block: Block[T, R] { type Env = Nothing }) extends DBlock[T, R] {
  type TheEnv = Nothing
  val duplicable = summon[Duplicable[Block[T, R] { type Env = TheEnv }]]
}
