package spores.pickle.test

import spores.Reflection.EnableReflectiveInstantiation
import spores.SporeBuilder


object SporeWithoutEnv extends SporeBuilder[Int => Int](
  (x: Int) => x + 1
)
