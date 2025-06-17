package spores.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import spores.Builder


@EnableReflectiveInstantiation
object SporeWithoutEnv extends Builder[Int, Int](
  (x: Int) => x + 1
)
