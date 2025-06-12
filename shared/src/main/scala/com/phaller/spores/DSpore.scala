package spores


object DSpore {

  def apply[T, R, B <: Spore[T, R]](spore: B)(using Duplicable[spore.Env]): DSpore[T, R] =
    new DSporeWithEnv[T, R](spore)

  def apply[T, R, B <: Spore[T, R] { type Env = Nothing }](spore: B): DSpore[T, R] =
    new DSporeWithoutEnv[T, R](spore)

}

trait DSpore[T, R] { self =>
  type Env
  def spore: Spore[T, R] { type Env = self.Env }
  def duplicable: Duplicable[Spore[T, R] { type Env = self.Env }]
  def duplicate(): Spore[T, R] { type Env = self.Env } =
    duplicable.duplicate(spore)
}

private class DSporeWithEnv[T, R](val s: Spore[T, R])(using Duplicable[s.Env]) extends DSpore[T, R] {
  type Env = s.Env
  val spore = s
  val duplicable = summon[Duplicable[Spore[T, R] { type Env = s.Env }]]
}

private class DSporeWithoutEnv[T, R](val spore: Spore[T, R] { type Env = Nothing }) extends DSpore[T, R] {
  type Env = Nothing
  val duplicable = summon[Duplicable[Spore[T, R] { type Env = Nothing }]]
}
