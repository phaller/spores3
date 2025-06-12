package spores.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import spores.Spore


@EnableReflectiveInstantiation
object AppendString extends
    Spore.Builder[String, List[String], List[String]](
  env => strings => strings ::: List(env)
)
