package com.phaller.spores.pickle.test

import com.phaller.spores.Builder


object SporeWithoutEnv extends Builder[Int, Int](
  (x: Int) => x + 1
)
