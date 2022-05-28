package com.phaller.blocks.pickle.test

import com.phaller.blocks.Block
import com.phaller.blocks.Block.checked


object MyBlock extends Block.Builder[Int, Int, Int](
  checked(x => env => env + x + 1)
)
