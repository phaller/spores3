package com.phaller.spores.pickle.test

import com.phaller.spores.Spore


object AppendString extends
    Spore.Builder[String, List[String], List[String]](
  env => strings => strings ::: List(env)
)
