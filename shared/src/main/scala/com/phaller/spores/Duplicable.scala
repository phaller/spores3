package spores


trait Duplicable[T]:
  def duplicate(value: T): T

object Duplicable {

  def duplicate[T](value: T)(using duplicable: Duplicable[T]): T =
    duplicable.duplicate(value)

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

}
