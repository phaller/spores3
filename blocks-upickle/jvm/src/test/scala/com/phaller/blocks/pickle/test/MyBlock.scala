package com.phaller.blocks.pickle.test

import com.phaller.blocks.Block


object MyBlock extends Block.Builder[Int, Int, Int](
  env => x => env + x + 1
)
