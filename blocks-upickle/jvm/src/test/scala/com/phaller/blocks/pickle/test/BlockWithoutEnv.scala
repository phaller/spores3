package com.phaller.blocks.pickle.test

import com.phaller.blocks.{Builder, checked}


object BlockWithoutEnv extends Builder[Int, Int](
  checked((x: Int) => x + 1)
)
