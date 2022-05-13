package com.phaller.blocks.pickle.test

import com.phaller.blocks.Block
import com.phaller.blocks.Block.env


object AppendString extends
    Block.Builder[String, List[String], List[String]](
  (strings: List[String]) => strings ::: List(env))
