package com.phaller.blocks.pickle.test

import com.phaller.blocks.Builder


object BlockWithoutEnv extends Builder[Int, Int](
  (x: Int) => x + 1
)
