package spores.jvm

import scala.annotation.targetName
import upickle.default.ReadWriter

import spores.*


/** Internal API. */
private[spores] object SporeJVM {

  // The Spore factory only works on the JVM. The generated class here is not a
  // top-level class. For this reason, it cannot be reflectively instantiated on
  // ScalaJS or ScalaNative. For more information, see:
  // https://github.com/portable-scala/portable-scala-reflect.

  inline def apply[T](inline body: T): Spore[T] = {
    SporeJVM0.apply(body)
  }

  inline def applyWithEnv[E, T](inline env: E)(inline body: E => T)(using rw: Spore[ReadWriter[E]]): Spore[T] = {
    apply[E => T](body).withEnv(env)
  }

  inline def applyWithCtx[E, T](inline env: E)(inline body: E ?=> T)(using rw: Spore[ReadWriter[E]]): Spore[T] = {
    apply[E ?=> T](body).withCtx(env)
  }

  inline def auto[T](inline body: T): Spore[T] = {
    SporeJVM0.auto(body)
  }
}
