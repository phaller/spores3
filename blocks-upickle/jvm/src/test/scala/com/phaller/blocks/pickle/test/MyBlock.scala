package com.phaller.blocks.pickle.test

import com.phaller.blocks.Block
import com.phaller.blocks.Block.{env, checked}


object MyBlock extends Block.Builder[Int, Int, Int](
  checked((x: Int) => env + x + 1)
)
