package com.phaller.blocks.pickle.test

import com.phaller.blocks.Block
import com.phaller.blocks.Block.{env, checked}


object AppendString extends
    Block.Builder[String, List[String], List[String]](
  checked((strings: List[String]) => strings ::: List(env))
)
