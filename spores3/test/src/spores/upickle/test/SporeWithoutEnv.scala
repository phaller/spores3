package spores.pickle.test

import spores.Reflection.EnableReflectiveInstantiation
import spores.SporeBuilder


object SporeWithoutEnv extends SporeBuilder[Int => Int] {
  override def body = x => x + 1
}
