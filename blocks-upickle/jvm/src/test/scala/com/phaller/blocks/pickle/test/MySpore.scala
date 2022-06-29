package com.phaller.blocks.pickle.test

import com.phaller.blocks.Spore


object MySpore extends Spore.Builder[Int, Int, Int](
  env => x => env + x + 1
)
