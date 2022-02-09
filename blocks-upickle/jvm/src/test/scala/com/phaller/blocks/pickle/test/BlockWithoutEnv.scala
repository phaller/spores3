package com.phaller.blocks.pickle.test

import com.phaller.blocks.Block


object BlockWithoutEnv extends Block.BuilderNoEnv[Nothing, Int, Int](
  (x: Int) => x + 1
)
