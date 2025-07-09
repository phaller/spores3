package spores.pickle.test

import spores.Reflection.EnableReflectiveInstantiation
import spores.Spore


@EnableReflectiveInstantiation
object AppendString extends
    Spore.Builder[String, List[String], List[String]](
  env => strings => strings ::: List(env)
)
