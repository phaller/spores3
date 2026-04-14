package spores


trait Duplicable[T]:
  def duplicate(value: T): T


object Duplicable {

  def duplicate[T](value: T)(using duplicable: Duplicable[T]): T =
    duplicable.duplicate(value)

  inline def apply[T](inline body: T): Spore0[Duplicable, T] = {
    spores.jvm.SporeJVM0.apply(body)
  }

  inline def applyWithEnv[E, T](env: E)(inline body: E => T)(using ev: Spore0[Duplicable, Duplicable[E]]): Spore0[Duplicable, T] = {
    apply[E => T](body).withEnv(duplicate(env)(using ev.get()))
  }

  inline def applyWithCtx[E, T](env: E)(inline body: E ?=> T)(using ev: Spore0[Duplicable, Duplicable[E]]): Spore0[Duplicable, T] = {
    apply[E ?=> T](body).withCtx(duplicate(env)(using ev.get()))
  }

  given [T]: Duplicable[Spore0[Duplicable, T]] = new Duplicable[Spore0[Duplicable, T]] {
    override def duplicate(value: Spore0[Duplicable, T]): Spore0[Duplicable, T] = {
      value match {
        case x @ Spore0.AST.Body(_, _, _)  => x
        case Spore0.AST.Value(ev, value)   => Spore0.AST.Value(ev, Duplicable.duplicate(value)(using ev.get()))
        case Spore0.AST.WithEnv(body, env) => Spore0.AST.WithEnv(Duplicable.duplicate(body), Duplicable.duplicate(env))
      }
    }
  }

  given Duplicable[Char] with
    def duplicate(value: Char): Char = value

  given Duplicable[Boolean] with
    def duplicate(value: Boolean): Boolean = value

  given Duplicable[Byte] with
    def duplicate(value: Byte): Byte = value

  given Duplicable[Short] with
    def duplicate(value: Short): Short = value

  given Duplicable[Int] with
    def duplicate(value: Int): Int = value

  given Duplicable[Long] with
    def duplicate(value: Long): Long = value

  given Duplicable[Float] with
    def duplicate(value: Float): Float = value

  given Duplicable[Double] with
    def duplicate(value: Double): Double = value

  given Duplicable[String] with
    def duplicate(value: String): String = value

  given [T](using d: Duplicable[T]): Spore0[Duplicable, Duplicable[T]] = {
    Spore0.AST.Body("", -1, d)
  }
}
