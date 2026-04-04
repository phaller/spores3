package spores.pickle.test

import spores.Reflection.EnableReflectiveInstantiation
import spores.SporeBuilder


object MySpore extends SporeBuilder[Int => Int => Int](
  env => x => env + x + 1
)
