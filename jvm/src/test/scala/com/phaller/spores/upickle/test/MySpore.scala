package spores.pickle.test

import spores.Spore


object MySpore extends Spore.Builder[Int, Int, Int](
  env => x => env + x + 1
)
