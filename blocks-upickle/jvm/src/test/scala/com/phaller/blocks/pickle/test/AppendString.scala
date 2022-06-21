package com.phaller.blocks.pickle.test

import com.phaller.blocks.Block


object AppendString extends
    Block.Builder[String, List[String], List[String]](
  env => strings => strings ::: List(env)
)
