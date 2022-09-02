package com.phaller.spores.pickle.test

import com.phaller.spores.Spore


object MySpore extends Spore.Builder[Int, Int, Int](
  env => x => env + x + 1
)
