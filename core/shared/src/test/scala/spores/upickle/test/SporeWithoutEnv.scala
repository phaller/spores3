package spores.pickle.test

import spores.Reflection.EnableReflectiveInstantiation
import spores.Builder


@EnableReflectiveInstantiation
object SporeWithoutEnv extends Builder[Int, Int](
  (x: Int) => x + 1
)
