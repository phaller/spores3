package spores.pickle.test

import spores.Reflection.EnableReflectiveInstantiation
import spores.SporeBuilder


object AppendString extends
    SporeBuilder[String => List[String] => List[String]](
  env => strings => strings ::: List(env)
)
