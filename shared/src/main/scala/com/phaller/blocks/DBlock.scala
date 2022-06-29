package com.phaller.blocks


object DBlock {

  def apply[T, R, B <: Spore[T, R]](spore: B)(using Duplicable[spore.Env]): DBlock[T, R] =
    new DBlockWithEnv[T, R](spore)

  def apply[T, R, B <: Spore[T, R] { type Env = Nothing }](spore: B): DBlock[T, R] =
    new DBlockWithoutEnv[T, R](spore)

}

trait DBlock[T, R] { self =>
  type Env
  def spore: Spore[T, R] { type Env = self.Env }
  def duplicable: Duplicable[Spore[T, R] { type Env = self.Env }]
  def duplicate(): Spore[T, R] { type Env = self.Env } =
    duplicable.duplicate(spore)
}

private class DBlockWithEnv[T, R](val b: Spore[T, R])(using Duplicable[b.Env]) extends DBlock[T, R] {
  type Env = b.Env
  val spore = b
  val duplicable = summon[Duplicable[Spore[T, R] { type Env = b.Env }]]
}

private class DBlockWithoutEnv[T, R](val spore: Spore[T, R] { type Env = Nothing }) extends DBlock[T, R] {
  type Env = Nothing
  val duplicable = summon[Duplicable[Spore[T, R] { type Env = Nothing }]]
}
