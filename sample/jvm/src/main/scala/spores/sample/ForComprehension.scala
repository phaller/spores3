package spores.sample

import spores.default.*
import spores.default.given

object ForComprehension {

  sealed trait Container[+A] {
    private[Container] val sporeOpt: Option[Spore[A]]

    inline def map[B](inline f: A => B): Container[B] = {
      Container.create(this.sporeOpt.map(x => Spore.auto(f).withEnv2(x)))
    }

    inline def flatMap[B](inline f: A => Container[B]): Container[B] = {
      this.sporeOpt.map(x => Spore.auto(f).withEnv2(x).unwrap()).getOrElse(Container.empty)
    }

    def withFilter(f: A => Boolean): Container[A] = {
      Container.create(this.sporeOpt.filter(x => f.apply(x.unwrap())))
    }

    def toOption: Option[A] = this.sporeOpt.map(_.unwrap())
  }

  object Container {
    private def create[A](so: Option[Spore[A]]): Container[A] =
      new Container[A] {
        private[Container] override val sporeOpt: Option[Spore[A]] = so
      }

    inline def apply[A](inline a: A): Container[A] = create(Some(Spore.auto(a)))
    def empty[A]: Container[A] = create(None)
  }

  def main(args: Array[String]): Unit = {
    val result = for {
      x <- Container(10)
      y <- Container(11)
      z <- Container(x + y)
    } yield x + y + z

    assert(result.toOption == Some(42))
    println(result.toOption)
  }
}
