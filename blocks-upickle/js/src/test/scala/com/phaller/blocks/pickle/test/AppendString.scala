package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.blocks.Block


@EnableReflectiveInstantiation
object AppendString extends
    Block.Builder[String, List[String], List[String]](
  env => strings => strings ::: List(env)
)
