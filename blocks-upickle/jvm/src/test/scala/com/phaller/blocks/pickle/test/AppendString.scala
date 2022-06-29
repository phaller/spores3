package com.phaller.blocks.pickle.test

import com.phaller.blocks.Spore


object AppendString extends
    Spore.Builder[String, List[String], List[String]](
  env => strings => strings ::: List(env)
)
