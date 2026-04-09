package spores.pickle.test

import spores.Reflection.EnableReflectiveInstantiation
import spores.SporeBuilder


object AppendString extends SporeBuilder[String => List[String] => List[String]] {
  override def body = env => strings => strings ::: List(env)
}
