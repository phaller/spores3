package com.phaller.blocks.pickle.test

import com.phaller.blocks.Block
import com.phaller.blocks.Block.checked


object AppendString extends
    Block.Builder[String, List[String], List[String]](
  checked(strings => env => strings ::: List(env))
)
