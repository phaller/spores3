package com.phaller.blocks.pickle.test

import com.phaller.blocks.Block


object MyBlock extends Block.Builder[Int, Int, Int](
  (x: Int) => Block.env + x + 1
)
