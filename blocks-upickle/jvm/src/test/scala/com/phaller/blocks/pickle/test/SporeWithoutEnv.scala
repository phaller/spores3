package com.phaller.blocks.pickle.test

import com.phaller.blocks.Builder


object SporeWithoutEnv extends Builder[Int, Int](
  (x: Int) => x + 1
)
