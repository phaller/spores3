package com.phaller.blocks


object DBlock2 {

  def apply[T, R, B <: Block[T, R]](block: B)(using Duplicable[block.Env]): DBlock2[T, R] =
    new DBlock2WithEnv[T, R](block)

  def apply[T, R, B <: Block[T, R] { type Env = Nothing }](block: B): DBlock2[T, R] =
    new DBlock2WithoutEnv[T, R](block)

}

trait DBlock2[T, R] {
  type TheEnv
  def block: Block[T, R] { type Env = TheEnv }
  def duplicable: Duplicable[Block[T, R] { type Env = TheEnv }]
  def duplicate(): Block[T, R] { type Env = TheEnv } =
    duplicable.duplicate(block)
}

class DBlock2WithEnv[T, R](val b: Block[T, R])(using Duplicable[b.Env]) extends DBlock2[T, R] {
  type TheEnv = b.Env
  val block = b
  val duplicable = summon[Duplicable[Block[T, R] { type Env = TheEnv }]]
}

class DBlock2WithoutEnv[T, R](val block: Block[T, R] { type Env = Nothing }) extends DBlock2[T, R] {
  type TheEnv = Nothing
  val duplicable = summon[Duplicable[Block[T, R] { type Env = TheEnv }]]
}
