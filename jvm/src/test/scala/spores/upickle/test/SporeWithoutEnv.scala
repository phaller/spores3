package spores.pickle.test

import spores.Builder


object SporeWithoutEnv extends Builder[Int, Int](
  (x: Int) => x + 1
)
